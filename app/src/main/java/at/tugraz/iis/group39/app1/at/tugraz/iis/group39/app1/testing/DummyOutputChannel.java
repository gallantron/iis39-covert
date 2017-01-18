package at.tugraz.iis.group39.app1.at.tugraz.iis.group39.app1.testing;

import java.io.IOException;

import at.tugraz.iis.group39.covert.CovertOutputChannel;

class DummyOutputChannel implements CovertOutputChannel
{
    final DummyInputChannel _recv;
    DummyOutputChannel(DummyInputChannel recv) { _recv = recv; }

    @Override
    public boolean isAvailable()
    {
        return true;
    }

    @Override
    public void sendBit(boolean bit) throws IOException
    {
        if (_closed)
            throw new IOException("Already closed.");
        _recv.receive(bit);
    }

    @Override
    public void close() throws IOException
    {
        _closed = true;
        _recv.close();
    }

    @Override
    public boolean isClosed()
    {
        return _closed;
    }

    private boolean _closed;
}
