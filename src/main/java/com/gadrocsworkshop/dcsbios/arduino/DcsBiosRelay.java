package com.gadrocsworkshop.dcsbios.arduino;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created by Craig Courtney on 8/26/2015.
 */
public class DcsBiosRelay implements SerialPortEventListener {

    public static void main(String[] args) {
        DcsBiosRelay relay = new DcsBiosRelay();
        relay.run();
    }

    private SerialPort serialPort;
    private int dcsPort = 7778;
    private DatagramSocket socket;
    private final byte[] sendBuffer = new byte[2048];
    private int sendBufferPointer = 0;
    private InetAddress dcsAddress = null;

    public DcsBiosRelay() {
        try {
            serialPort = new SerialPort("COM9");
            serialPort.openPort();
            serialPort.setParams(250000, 8, 1, 0);
            serialPort.addEventListener(this);

            InetAddress group = InetAddress.getByName("239.255.50.10");
            socket = new MulticastSocket(5010);
            ((MulticastSocket)socket).joinGroup(group);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void run() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            while(true) {
                socket.receive(packet);
                dcsAddress = packet.getAddress();
                int size = packet.getLength();
                for(int i=0;i<size;i++) {
                    serialPort.writeByte(buf[packet.getOffset()+i]);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            try {
                int count = serialPortEvent.getEventValue();
                byte[] data = serialPort.readBytes(count);
                for (byte aData : data) {
                    sendBuffer[sendBufferPointer++] = aData;
                    if (aData == 10) {
                        if (dcsAddress != null) {
                            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBufferPointer, dcsAddress, dcsPort);
                            socket.send(sendPacket);
                        }
                        sendBufferPointer = 0;
                    }
                }
            } catch (SerialPortException | IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
