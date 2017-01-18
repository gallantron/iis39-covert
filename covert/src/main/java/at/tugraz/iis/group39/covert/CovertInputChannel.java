package at.tugraz.iis.group39.covert;

import java.io.IOException;

public interface CovertInputChannel
{
     interface Callback
    {
        void onBitReceived(boolean bit);
        void onClosed();
    }

    // do checks on whether the current device supports this channel
    // stuff like "are there sufficient volume settings" etc
    boolean isAvailable();

    // whether the stream is closed
    boolean isClosed();
    // sets a CovertInputChannel.Callback object that should have its methods invoked
    void setCallback(Callback callback);
}
