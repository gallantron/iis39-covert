package at.tugraz.iis.group39.covertstreams;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

import at.tugraz.iis.group39.covert.CovertOutputChannel;

public class RawCovertChannelOutputStream extends OutputStream
{
    private final CovertOutputChannel _channel;
    public RawCovertChannelOutputStream(CovertOutputChannel channel) { _channel = channel; }

    @Override
    public void write(int b) throws IOException
    {
        if (_channel.isClosed())
            throw new IOException("Channel is already closed.");

        for (byte offset = 0; offset < 8; ++offset)
        {
            Log.d("SEND", String.format("@%d: %s (%s)", offset, (b & (1 << offset)) > 0 ? "1" : "0", Integer.toBinaryString(b)));
            _channel.sendBit((b & (1 << offset)) != 0);
        }
    }

    @Override
    public void close() throws IOException
    {
        if (_channel.isClosed())
            throw new IOException("Channel is already closed.");
        _channel.close();
        Log.d("OUT CLOSE", "Closed");
    }
}
