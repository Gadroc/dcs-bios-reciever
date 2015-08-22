package com.gadrocsworkshop.dcsbios.receiver;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * Receiver which listens for DCSBIOS UDP packets and processes allows listening to the stream.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
public class DcsBiosUdpReceiver implements DcsBiosReceiver {

    private final static Logger LOGGER = Logger.getLogger(DcsBiosUdpReceiverThread.class.getName());

    private DcsBiosParser parser;
    private DcsBiosUdpReceiverThread thread;

    /**
     * Creates a new receiver with the default DCSBIOS group and port addresses.
     */
    public DcsBiosUdpReceiver() throws IOException {
        this("239.255.50.10", 5010);
    }

    /**
     * Creates a new receiver which listens only for packets sent to this computer
     * on the designated port.
     *
     * @param port Port to listen on for DCSBIOS packets.
     */
    public DcsBiosUdpReceiver(int port) throws IOException {
        this(null, port);
    }

    /**
     * Creates a new receiver.
     *
     * @param groupAddress Multicast group address we should listen on.
     *                     If null or empty will only listen for packets sent directly to this computer.
     * @param port Port to listen on for DCSBIOS packets.
     */
    public DcsBiosUdpReceiver(String groupAddress, int port) throws IOException {
        this.parser = new DcsBiosParser();
        this.thread = new DcsBiosUdpReceiverThread(this.parser, groupAddress, port);
    }

    /**
     * Address of the DCSBIOS server we are receiving packets from.
     *
     * @return InetAddress object containing address of the DCSBIOS server. Null if we have not received any packets yet.
     */
    public InetAddress getDcsAddress() {
        return thread.getDcsAddress();
    }

    /**
     * Checks to see if the UDP reciever is running and processing data.
     *
     * @return True if the receiver is running, false otherwise.
     */
    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    @Override
    public void start() {
        if (!isRunning()) {
            thread.setRunning(true);
            thread.start();
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            thread.setRunning(false);
        }
    }

    @Override
    public void sendCommand(String command) throws IOException {
        thread.sendCommand(command);
    }

    @Override
    public void sendCommand(byte[] command) throws IOException {
        thread.sendCommand(command);
    }

    @Override
    public void addDataListener(DcsBiosDataListener listener) {
        parser.addDataListener(listener);
    }

    @Override
    public void removeDataListener(DcsBiosDataListener listener) {
        parser.removeDataListener(listener);
    }

    @Override
    public void addSyncListener(DcsBiosSyncListener listener) {
        parser.addSyncListener(listener);
    }

    @Override
    public void removeSyncListener(DcsBiosSyncListener listener) {
        parser.removeSyncListener(listener);
    }
}
