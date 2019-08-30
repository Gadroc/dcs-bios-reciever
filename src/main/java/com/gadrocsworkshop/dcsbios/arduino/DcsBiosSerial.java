package com.gadrocsworkshop.dcsbios.arduino;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DcsBiosSerial {

    public static void main(String[] args) {
        DcsBiosSerial relay = new DcsBiosSerial(args[0]);
        relay.run();
    }

    private SerialPort serialPort;
    private int dcsPort = 7778;
    private DatagramSocket socket;
    private final byte[] sendBuffer = new byte[2048];
    private int sendBufferPointer = 0;
    private InetAddress dcsAddress = null;

    private SerialPortDataListener listener = new SerialPortDataListener() {
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

        @Override
        public void serialEvent(SerialPortEvent event)
        {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                return;

            try {
                byte[] data = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(data, data.length);
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
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    };

    public DcsBiosSerial(String portName) {
        try {
            serialPort = SerialPort.getCommPort(portName);
            serialPort.openPort();
            serialPort.setComPortParameters(250000, 8, 1, 0);
            serialPort.addDataListener(listener);

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
                serialPort.writeBytes(buf, size, packet.getOffset());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
