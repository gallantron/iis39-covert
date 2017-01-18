package at.tugraz.iis.group39.covertstreams;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import at.tugraz.iis.group39.covert.CovertInputChannel;

public class RawCovertChannelInputStream extends InputStream
{
    private final CovertInputChannel _channel;
    public RawCovertChannelInputStream(CovertInputChannel channel) { _channel = channel; _channel.setCallback(new Callback()); }

    @Override
    public int read() throws IOException
    {
        Byte data = _queue.poll();
        while (data == null)
        {
            if (_channel.isClosed())
                return -1;
            else
                try { data = _queue.poll(1, TimeUnit.SECONDS); }
                catch (InterruptedException e) { data = null; }
        }
        return data;
    }

    class Callback implements CovertInputChannel.Callback
    {
        @Override
        public void onBitReceived(boolean bit)
        {
            if (bit)
                _currentByte |= (1 << _recvOffset);

            Log.d("RECV", String.format("@%d: %s (%s)", _recvOffset, bit ? "1" : "0", Integer.toBinaryString(_currentByte)));

            ++_recvOffset;
            if (_recvOffset == 8)
            {
                try { _queue.put(_currentByte); }
                catch (Exception e) { Log.d("CHANNEL", String.format("Exception pushing received byte %d.", _currentByte), e); }
                _currentByte = 0;
                _recvOffset = 0;
            }
        }

        @Override
        public void onClosed() { Log.d("INPUT CLOSE", _channel.isClosed() ? "1" : "0"); }
    }

    private LinkedBlockingQueue<Byte> _queue = new LinkedBlockingQueue<>();
    private byte _currentByte = 0;
    private byte _recvOffset = 0;
}
