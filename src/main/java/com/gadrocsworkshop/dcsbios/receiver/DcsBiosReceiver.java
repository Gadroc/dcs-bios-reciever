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
     * Registers a data listener to this DCS-BIOS receiver.  Data listeners are
     * notified anytime new data is read from the DCS-BIOS stream.
     *
     * @param listener Listener which will get notified
     */
    void addDataListener(DcsBiosDataListener listener);

    /**
     * Removes a data listener from this DCS-BIOS receiver.
     *
     * @param listener Listener which will no longer be notified of new data.
     */
    void removeDataListener(DcsBiosDataListener listener);

    /**
     * Registers a sync listener to this DCS-BIOS receiver.  Sync listeners are
     * notified at the end of a frame of data.  Data is only in a consistent state
     * during a sync call.  Any data read outside of a sync call may be invalid
     * data.
     *
     * @param listener Listener to get sync events.
     */
    void addSyncListener(DcsBiosSyncListener listener);

    /**
     * Removes a sync listener from this DCS-BIOS receiver.
     *
     * @param listener Listener which will no longer be notified of sync events.
     */
    void removeSyncListener(DcsBiosSyncListener listener);

    /**
     * Registers a stream listener to this DCS-BIOS receiver.  Stream listeners are
     * notified of raw stream data.
     *
     * @param listener Listener to get sync events.
     */
    void addStreamListener(DcsBiosStreamListener listener);

    /**
     * Removes a stream listener from this DCS-BIOS receiver.
     *
     * @param listener Listener which will no longer be notified of sync events.
     */
    void removeStreamListener(DcsBiosStreamListener listener);

    /**
     * Sends a command back to the DCS-BIOS
     *
     * @param command Command to send to DCS-BIOS
     * @throws IOException Thrown if an error occurs sending the datagram.
     */
    void sendCommand(String command) throws IOException;

    /**
     * Sends a command back to the DCS-BIOS
     *
     * @param command Command to send to DCS-BIOS
     * @throws IOException Thrown if an error occurs sending the datagram.
     */
    void sendCommand(byte[] command) throws IOException;
}
