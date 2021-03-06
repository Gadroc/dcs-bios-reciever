package com.gadrocsworkshop.dcsbios.receiver;

import java.io.IOException;
import java.net.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDP Listener thread for DCS-BIOS packets.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
class DcsBiosUdpReceiverThread extends Thread {

    private final static Logger LOGGER = Logger.getLogger(DcsBiosUdpReceiverThread.class.getName());

    private int dcsPort = 7778;
    private DatagramSocket socket;
    private boolean running = true;
    private InetAddress dcsAddress = null;
    private final DcsBiosParser parser;

    private final LinkedHashSet<DcsBiosStreamListener> streamListeners = new LinkedHashSet<>();

    /**
     * Creates a new receiver thread.
     *
     * @param parser Parser to use with this UDP receiver.
     * @param groupAddress Multicast group address we should listen on.
     *                     If null or empty will only listen for packets sent directly to this computer.
     * @param port Port to listen on for DCS-BIOS packets.
     * @throws IOException Thrown if there are problems creating DatagramSocket
     */
    public DcsBiosUdpReceiverThread(DcsBiosParser parser, String groupAddress, int port) throws IOException {
        this.parser = parser;

        if (groupAddress == null || groupAddress.trim().isEmpty()) {
            socket = new DatagramSocket(port);
            LOGGER.fine("DatagramSocket created and bound to address.");
        }
        else {
            InetAddress group = InetAddress.getByName(groupAddress);
            socket = new MulticastSocket(port);
            ((MulticastSocket)socket).joinGroup(group);
            LOGGER.fine("MulticastSocket created and joined to group address.");
        }

        socket.setSoTimeout(1000);
    }

    /**
     * Registers a stream listener to this DCS-BIOS parser.  Stream listeners are
     * notified incoming raw stream data.
     *
     * @param listener Listener to get stream events.
     */
    public synchronized void addStreamListener(DcsBiosStreamListener listener) {
        if (listener == null) {
            throw new NullPointerException("Can't add null listener.");
        }
        streamListeners.add(listener);
    }

    /**
     * Removes a stream listener from this DCS-BIOS parser.
     *
     * @param listener Listener which will no longer be notified of stream events.
     */
    public synchronized void removeStreamListener(DcsBiosStreamListener listener) {
        streamListeners.remove(listener);
    }

    /**
     * Address of the DCS-BIOS server we are receiving packets from.
     *
     * @return InetAddress object containing address of the DCS-BIOS server. Null if we have not received any packets yet.
     */
    public InetAddress getDcsAddress() {
        return dcsAddress;
    }

    /**
     * Port which commands are sent to DCS-BIOS on.
     *
     * @return Port number used to send DCS-BIOS commands to.
     */
    public int getDcsPort() {
        return dcsPort;
    }

    /**
     * Sets port number which commands will be sent to DCS-BIOS on.
     *
     * @param dcsPort Port number used to send DCS-BIOS commands to.
     */
    public void setDcsPort(int dcsPort) {
        this.dcsPort = dcsPort;
    }

    /**
     * Run loop for receiving packets.
     */
    public void run() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        LOGGER.fine("Entering packet reading loop.");
        while(running) {
            try {
                socket.receive(packet);
                if (running) {
                    dcsAddress = packet.getAddress();
                    parser.processData(buf, packet.getOffset(), packet.getLength());
                    notifyStreamListeners(buf, packet.getOffset(), packet.getLength());
                }
            }
            catch (SocketTimeoutException e) {
                // Ignore timeout and receive again.  Timeout
                // is there to facilitate canceling the running thread.
                // LOGGER.finest("Timeout waiting for packet.");
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error receiving packet from DCS Bios. Shutting down listener.", e);
                running = false;
            }
        }

        LOGGER.fine("Exiting packet reading loop.");
        socket.close();
    }

    /**
     * Sets the internal flag determining if the run loop should continue.  Should be set to true
     * before calling start on this thread.
     *
     * @param running True if the UDP thread should continue, false if it should exit.
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

    /**
     * Sends a command back to the DCS-BIOS
     *
     * @param command Command to send to DCS-BIOS
     * @throws IOException Thrown if an error occurs sending the datagram.
     */
    public void sendCommand(String command) throws IOException {
        sendCommand(command.getBytes());
    }

    /**
     * Sends a command back to the DCS-BIOS
     *
     * @param command Command to send to DCS-BIOS
     * @throws IOException Thrown if an error occurs sending the datagram.
     */
    public void sendCommand(byte[] command) throws IOException {
        if (running && dcsAddress != null && socket != null && command != null) {
            DatagramPacket sendPacket = new DatagramPacket(command, command.length, dcsAddress, dcsPort);
            socket.send(sendPacket);
        }
    }

    /**
     * Helper method which notifies all packet listeners.
     */
    private void notifyStreamListeners(byte[] data, int offset, int length) {
        Set<DcsBiosStreamListener> s;
        synchronized (this) {
            s = new LinkedHashSet<>(streamListeners);
        }
        for(DcsBiosStreamListener listener : s) {
            try {
                listener.dcsBiosStreamDataReceived(data, offset, length);
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, String.format("Exception thrown from DCS-BIOS packet handler %s.", listener.getClass().getName()), ex);
            }
        }
    }
}
