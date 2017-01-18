package at.tugraz.iis.group39.app1.at.tugraz.iis.group39.app1.testing;

import at.tugraz.iis.group39.covert.CovertInputChannel;

class DummyInputChannel implements CovertInputChannel
{
    @Override
    public void setCallback(Callback callback)
    {
        _callback = callback;
    }

    @Override
    public boolean isAvailable()
    {
        return true;
    }

    @Override
    public boolean isClosed()
    {
        return _closed;
    }

    void close()
    {
        _closed = true;
        _callback.onClosed();
    }

    void receive(boolean bit)
    {
        _callback.onBitReceived(bit);
    }

    private Callback _callback;
    private boolean _closed;
}
