package com.gadrocsworkshop.dcsbios;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDP Listener thread for DCSBIOS packets.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
class DcsBiosUdpReceiverThread extends Thread {

    private final static Logger LOGGER = Logger.getLogger(DcsBiosUdpReceiverThread.class.getName());

    private int dcsPort = 7778;
    private DatagramSocket socket;
    private boolean running = true;
    private InetAddress dcsAddress = null;
    private DcsBiosParser parser;

    /**
     * Creates a new receiver thread.
     *
     * @param parser Parser to use with this UDP reciever.
     * @param groupAddress Multicast group address we should listen on.
     *                     If null or empty will only listen for packets sent directly to this computer.
     * @param port Port to listen on for DCSBIOS packets.
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
     * Address of the DCSBIOS server we are receiving packets from.
     *
     * @return InetAddress object containing address of the DCSBIOS server. Null if we have not received any packets yet.
     */
    public InetAddress getDcsAddress() {
        return dcsAddress;
    }

    /**
     * Port which commands are sent to DCSBIOS on.
     *
     * @return Port number used to send DCSBIOS commands to.
     */
    public int getDcsPort() {
        return dcsPort;
    }

    /**
     * Sets port number which commadns will be sent to DCSBIOS on.
     *
     * @param dcsPort Port number used to send DCSBIOS commands to.
     */
    public void setDcsPort(int dcsPort) {
        this.dcsPort = dcsPort;
    }

    /**
     * Run loop for receiving packets.
     */
    public void run() {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        LOGGER.fine("Entering packet reading loop.");
        while(running) {
            try {
                socket.receive(packet);
                if (running) {
                    dcsAddress = packet.getAddress();
                    parser.processData(buf, packet.getOffset(), packet.getLength());
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
     * Sends a command back to the DCSBIOS
     *
     * @param command Command to send to DCSBIOS
     * @throws IOException Thrown if an error ocurrs sending the datagram.
     */
    public void sendCommand(String command) throws IOException {
        if (running && dcsAddress != null && socket != null && command != null) {
            byte[] sendData = command.getBytes();
            if (sendData != null) {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, dcsAddress, dcsPort);
                socket.send(sendPacket);
            }
        }
    }
}
