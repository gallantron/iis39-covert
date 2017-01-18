package at.tugraz.iis.group39.covert;


import android.util.Log;

import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;

public class EncodedPipe {
    private static final char DATA_PER_CHUNK = 32;
    private static final byte METADATA_PER_CHUNK = 3; // 1 byte checksum, 2 bytes index
    private static final char BYTES_PER_CHUNK = DATA_PER_CHUNK + METADATA_PER_CHUNK;
    private static final byte[] EOF = new byte[0];

    class RecvCallback implements CovertChannel.Callback
    {
        RecvCallback(EncodedPipe pipe) { _pipe = pipe; }
        @Override
        public void onBitReceived(boolean bit)
        {
            _pipe.onBitReceived(bit);
        }
        private EncodedPipe _pipe;
    }

    class IncomingDataStream extends InputStream
    {
        @Override
        public int read()
        {
            if (_currentByte >= BYTES_PER_CHUNK)
            { // get next chunk from queue
                _currentByte = METADATA_PER_CHUNK; // skip metadata

                _current = _queue.poll();
                if (_current == null)
                    if (_hasEOF)
                        return -1;
                    else
                    {
                        try { _current = _queue.take(); }
                        catch (Exception e)
                        {
                            Log.d("DEBUG", "Exception getting data from stream queue", e);
                            return -1;
                        }
                    }
            }
            return _current[_currentByte++];
        }

        @Override
        public int available()
        {
            return _queue.size() * DATA_PER_CHUNK;
        }

        void push(byte[] arr)
        {
            try { _queue.put(arr); }
            catch (Exception e) { Log.d("DEBUG", "Exception pushing to data stream queue", e); }
        }

        void setEOF()
        {
            _hasEOF = true;
        }

        private byte[] _current;
        private int _currentByte = BYTES_PER_CHUNK;
        private LinkedBlockingQueue<byte[]> _queue = new LinkedBlockingQueue<>();
        private boolean _hasEOF = false;
    }

    static byte calculateChecksum(byte[] data, int initialIndex)
    {
        byte checksum = 0;
        for (int i=0; i < METADATA_PER_CHUNK*8; ++i)
            // if the i-th bit is set...
            if ((data[initialIndex + (i/8)] & ((byte)1 << (i%8))) != 0)
                for (int b = 0; b < 8; ++b)
                    if (i%(b+1) == 0) // ...and its index is a multiple of b+1...
                        checksum ^= ((byte)1 << b); // ...flip the b-th byte in checksum
        return checksum;
    }

    public EncodedPipe(CovertChannel channel)
    {
        _channel = channel;
        channel.setReceiveCallback(new RecvCallback(this));
    }

    private void onBitReceived(boolean bit)
    {
        if (_currentRecvData == null)
            _currentRecvData = new byte[BYTES_PER_CHUNK];

        if (bit)
            _currentRecvData[_currentByteIndex] |= ((byte)1 << _offsetWithinByte);

        // case 1: in the middle of a byte
        if (_offsetWithinByte < 7)
        {
            ++_offsetWithinByte;
            return;
        }
        else
            _offsetWithinByte = 0;

        // case 2: in the middle of a chunk
        if (_currentByteIndex < BYTES_PER_CHUNK)
        {
            ++_currentByteIndex;
            return;
        }
        else
            _currentByteIndex = 0;

        // case 3: last bit in chunk, check index+checksum
        short sentChunkIndex = (short)((_currentRecvData[1] << 8) | _currentRecvData[2]);
        byte checksum = calculateChecksum(_currentRecvData, METADATA_PER_CHUNK);
        byte sentChecksum = _currentRecvData[0];
        boolean success = (sentChunkIndex == _nextChunkIndex);
        if (success)
        {
            if (checksum == sentChecksum)
            {
                // checksum matched, not EOF yet
                _outStream.push(_currentRecvData);
            }
            else if (~checksum == sentChecksum)
            {
                // checksum matched, EOF
                _outStream.push(_currentRecvData);
                _outStream.setEOF();

                // reset input stream + index
                _nextChunkIndex = -1;
                _outStream = new IncomingDataStream();
            }
            else
                success = false;
        }

        if (success)
        {
            ++_nextChunkIndex;
            // signal success to sender
        }
        else
        {
            // mismatch
            Log.d("TRANSMIT", String.format("Error receiving chunk %d, index/checksum mismatch (%d vs %d, %d vs %d).", _nextChunkIndex, sentChecksum, _nextChunkIndex, sentChecksum, checksum));
        }
    }

    final CovertChannel _channel;

    /* receiving: current chunk */
    private char _offsetWithinByte = 0; // position of next bit
    private char _currentByteIndex = 0; // array index of current byte (0 to BYTES_PER_CHUNK)
    private byte[] _currentRecvData = null;

    /* receiving: across chunks */
    private short _nextChunkIndex = 0;
    private IncomingDataStream _outStream = new IncomingDataStream();
}
