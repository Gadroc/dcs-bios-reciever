package com.gadrocsworkshop.dcsbios.receiver;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser for the DCS-BIOS network protocol.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
public class DcsBiosParser {

    private static final Logger LOGGER = Logger.getLogger(DcsBiosParser.class.getName());

    private enum ParserState {
        WAIT_FOR_SYNC, ADDRESS_LOW, ADDRESS_HIGH, COUNT_LOW, COUNT_HIGH, DATA_LOW, DATA_HIGH
    }

    private byte syncByteCount = 0;
    private int address = 0;
    private int remaining = 0;
    private int value = 0;

    private ParserState state = ParserState.WAIT_FOR_SYNC;

    private final LinkedHashSet<DcsBiosDataListener> dataListeners = new LinkedHashSet<>();
    private final LinkedHashSet<DcsBiosSyncListener> syncListeners = new LinkedHashSet<>();

    /**
     * Registers a data listener to this DCS-BIOS parser.  Data listeners are
     * notified anytime new data is read from the DCS-BIOS stream.
     *
     * @param listener Listener which will get notified
     */
    public synchronized void addDataListener(DcsBiosDataListener listener) {
        if (listener == null) {
            throw new NullPointerException("Can't add null listener.");
        }
        dataListeners.add(listener);
    }

    /**
     * Removes a data listener from this DCS-BIOS parser.
     *
     * @param listener Listener which will no longer be notified of new data.
     */
    public synchronized void removeDataListener(DcsBiosDataListener listener) {
        dataListeners.remove(listener);
    }

    /**
     * Registers a sync listener to this DCS-BIOS parser.  Sync listeners are
     * notified at the end of a frame of data.  Data is only in a consistent state
     * during a sync call.  Any data read outside of a sync call may be invalid
     * data.
     *
     * @param listener Listener to get sync events.
     */
    public synchronized void addSyncListener(DcsBiosSyncListener listener) {
        if (listener == null) {
            throw new NullPointerException("Can't add null listener.");
        }
        syncListeners.add(listener);
    }

    /**
     * Removes a sync listener from this DCS-BIOS parser.
     *
     * @param listener Listener which will no longer be notified of sync events.
     */
    public synchronized void removeSyncListener(DcsBiosSyncListener listener) {
        syncListeners.remove(listener);
    }

    /**
     * Process a buffer containing DCS-BIOS stream data.
     *
     * @param buffer Buffer containing the DCS-BIOS stream.
     * @param offset Offset into the buffer to start processing.
     * @param length Number of bytes to process.
     */
    public void processData(byte[] buffer, int offset, int length) {
        for (int i=0; i<length; i++) {
            processData(buffer[offset+i]);
        }
    }

    /**
     * Process the next byte of a DCS bios stream.
     *
     * @param data Byte to process.
     */
    @SuppressWarnings("WeakerAccess")
    public void processData(byte data) {
        switch (state) {
            case WAIT_FOR_SYNC:
                // Sync markers are processed outside loop no mater
                // what state the parser is in.  See end of switch.
                break;

            case ADDRESS_LOW:
                address = data & 0xff;
                state = ParserState.ADDRESS_HIGH;
                break;

            case ADDRESS_HIGH:
                address += (data & 0xff) << 8;
                if (address == 0x5555) {
                    state = ParserState.WAIT_FOR_SYNC;
                }
                else {
                    state = ParserState.COUNT_LOW;
                }
                break;

            case COUNT_LOW:
                remaining = data & 0xff;
                state = ParserState.COUNT_HIGH;
                break;

            case COUNT_HIGH:
                remaining += (data & 0xff) << 8;
                state = ParserState.DATA_LOW;
                break;

            case DATA_LOW:
                value = data & 0xff;
                remaining--;
                state = ParserState.DATA_HIGH;
                break;

            case DATA_HIGH:
                value += (data & 0xff) << 8;
                remaining--;
                if (remaining == 0) {
                    state = ParserState.ADDRESS_LOW;
                    if (address == 0xfffe) {
                        notifySyncListeners();
                    }
                    else {
                        notifyDataListeners();
                    }
                }
                else {
                    notifyDataListeners();
                    address += 2;
                    state = ParserState.DATA_LOW;
                }
                break;
        }

        // We need to check for sync markers out side state in case we have lost data.
        // Transmitter of stream is responsible for preventing sync marker pattern is
        // present in real data.
        if (data == 0x55) {
            syncByteCount++;
            // When we have 4 sync bytes in a row start looking for an address.
            if (syncByteCount == 4) {
                state = ParserState.ADDRESS_LOW;
                syncByteCount = 0;
            }
        }
        else {
            syncByteCount = 0;
        }
    }

    /**
     * Helper method which notifies all data listeners.
     */
    private void notifyDataListeners() {
        Set<DcsBiosDataListener> s;
        synchronized (this) {
            s = new LinkedHashSet<>(dataListeners);
        }
        for(DcsBiosDataListener listener : s) {
            try {
                listener.dcsBiosDataWritten(address, value);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, String.format("Exception thrown from DCS-BIOS data handler %s.", listener.getClass().getName()), ex);
            }
        }
    }

    /**
     * Helper method which notifies all sync listeners.
     */
    private void notifySyncListeners() {
        Set<DcsBiosSyncListener> s;
        synchronized (this) {
            s = new LinkedHashSet<>(syncListeners);
        }
        for(DcsBiosSyncListener listener : s) {
            try {
                listener.dcsBiosFrameSyncReceived();
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, String.format("Exception thrown from DCS-BIOS sync handler %s.", listener.getClass().getName()), ex);
            }
        }
    }
}
