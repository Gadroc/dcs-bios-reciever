package com.gadrocsworkshop.dcsbios.receiver;

import java.io.IOException;

public interface DcsBiosReceiver {

    /**
     * Starts the receiver listening for data from DCS.
     */
    void start();

    /**
     * Stops the receiver from listening for data and processing.
     */
    void stop();

    /**
     * Registers a data listener to this DCSBIOS receiver.  Data listeners are
     * notified anytime new data is read from the DCSBIOS stream.
     *
     * @param listener Listener which will get notified
     */
    void addDataListener(DcsBiosDataListener listener);

    /**
     * Removes a data listener from this DCSBIOS receiver.
     *
     * @param listener Listener which will no longer be notified of new data.
     */
    void removeDataListener(DcsBiosDataListener listener);

    /**
     * Registers a sync listener to this DCSBIOS receiver.  Sync listeners are
     * notified at the end of a frame of data.  Data is only in a consistent state
     * during a sync call.  Any data read outside of a sync call may be invalid
     * data.
     *
     * @param listener Listener to get sync events.
     */
    void addSyncListener(DcsBiosSyncListener listener);

    /**
     * Removes a sync listener from this DCSBIO receiver.
     *
     * @param listener Listener which will no longer be notified of sync events.
     */
    void removeSyncListener(DcsBiosSyncListener listener);

    /**
     * Sends a command back to the DCSBIOS
     *
     * @param command Command to send to DCSBIOS
     * @throws IOException Thrown if an error ocurrs sending the datagram.
     */
    void sendCommand(String command) throws IOException;

    /**
     * Sends a command back to the DCSBIOS
     *
     * @param command Command to send to DCSBIOS
     * @throws IOException Thrown if an error ocurrs sending the datagram.
     */
    void sendCommand(byte[] command) throws IOException;
}
