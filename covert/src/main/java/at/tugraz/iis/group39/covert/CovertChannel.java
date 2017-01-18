package at.tugraz.iis.group39.covert;

public interface CovertChannel
{
     interface Callback
    {
        void onBitReceived(boolean bit);
    }

    // do checks on whether the current device supports this channel
    // stuff like "are there sufficient volume settings" etc
    boolean isAvailable();

    int update();
    void sendBit(boolean bit);
    void setReceiveCallback(Callback callback);
}
