package com.gadrocsworkshop.dcsbios;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UDP Listener thread for DCSBIOS packets.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
class DcsBiosUdpReceiverThread extends Thread {

    private final static Logger LOGGER = Logger.getLogger(DcsBiosUdpReceiverThread.class.getName());

    private MulticastSocket socket;
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
        socket = new MulticastSocket(port);
        InetAddress group = InetAddress.getByName(groupAddress);
        LOGGER.finest("DatagramSocket created and bound to address.");
        if (groupAddress != null && groupAddress.trim().isEmpty()) {
            socket.joinGroup(group);
            socket.setSoTimeout(1000);
            LOGGER.finest("DatagramSocket joined to group address.");
        }
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
     * Run loop for receiving packets.
     */
    public void run() {
        byte[] buf = new byte[256];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

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
            }
            catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Error receiving packet from DCS Bios. Shutting down listener.", e);
                running = false;
            }
        }

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
}
