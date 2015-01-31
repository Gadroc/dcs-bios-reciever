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
    private DcsBiosParser parser = new DcsBiosParser();

    /**
     * Creates a new receiver thread with the default DCSBIOS group and port addresses.
     * @throws IOException Thrown if there are problems creating DatagramSocket
     */
    public DcsBiosUdpReceiverThread() throws IOException {
        this("239.255.50.10", 5010);
    }

    /**
     * Creates a new receiver which listens only for packets sent to this computer
     * on the designated port.
     *
     * @param port Port to listen on for DCSBIOS packets.
     * @throws IOException Thrown if there are problems creating DatagramSocket
     */
    public DcsBiosUdpReceiverThread(int port) throws IOException {
        this(null, port);
    }

    /**
     * Creates a new receiver.
     *
     * @param groupAddress Multicast group address we should listen on.
     *                     If null or empty will only listen for packets sent directly to this computer.
     * @param port Port to listen on for DCSBIOS packets.
     * @throws IOException Thrown if there are problems creating DatagramSocket
     */
    public DcsBiosUdpReceiverThread(String groupAddress, int port) throws IOException {
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
                dcsAddress = packet.getAddress();
                parser.processData(buf, packet.getOffset(), packet.getLength());
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
}
