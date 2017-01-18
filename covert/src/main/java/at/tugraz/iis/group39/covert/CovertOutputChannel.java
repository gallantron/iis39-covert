package at.tugraz.iis.group39.covert;

import java.io.IOException;

public interface CovertOutputChannel
{
    // do checks on whether the current device supports this channel
    // stuff like "are there sufficient volume settings" etc
    boolean isAvailable();

    // schedules a bit for sending - the channel must maintain an internal queue of bits to send
    void sendBit(boolean bit) throws IOException;
    // closes the stream after all bits in current queue have been sent
    void close() throws IOException;
    // whether the stream is closed
    boolean isClosed();
}
