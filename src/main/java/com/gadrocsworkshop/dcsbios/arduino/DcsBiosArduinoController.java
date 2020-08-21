package com.gadrocsworkshop.dcsbios.arduino;

import com.fazecast.jSerialComm.SerialPort;

import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosStreamListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller which speaks with a controller using the DcsBiosArduino library.
 */
public class DcsBiosArduinoController implements DcsBiosStreamListener {

    private static final Logger LOGGER = Logger.getLogger(DcsBiosArduinoController.class.getName());

    private static final byte COMMAND_REQUEST_STATUS = (byte)'s';
    private static final byte COMMAND_LOAD_EXPORT_DATA = (byte)'e';

    private static final byte CONTROLLER_READY_FOR_DATA = (byte)'r';
    private static final byte CONTROLLER_BUFFER_FULL = (byte)'t';
    private static final byte CONTROLLER_DATA_RECEIVED = (byte)'v';
    private static final byte CONTROLLER_ERROR_LOADING = (byte)'x';
    private static final byte CONTROLLER_MESSAGE_RECEIVED = (byte)'m';

    private enum CONTROLLER_STATE {
        WAITING,
        MESSAGE_SIZE,
        MESSAGE_DATA
    }

    private final DcsBiosReceiver receiver;
    private final ByteRingBuffer buffer;

    private String serialPortName;
    private SerialPort serialPort;
    private byte[] writeBuffer = new byte[67];

    private boolean statusRequestPending;
    private boolean controllerReadyForData;
    private CONTROLLER_STATE state;

    private int messageSize;
    private int messagePointer;
    private final byte[] messageBuffer = new byte[64];

    private SerialPortDataListener listener = new SerialPortDataListener() {
        @Override
        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }

        @Override
        public void serialEvent(SerialPortEvent event) {
            byte[] data = new byte[serialPort.bytesAvailable()];
            int numRead = serialPort.readBytes(data, data.length);
            for (byte datum : data) {
                switch (state) {
                    case WAITING:
                        processControllerNotification(datum);
                        break;
                    case MESSAGE_SIZE:
                        messageSize = datum;
                        state = CONTROLLER_STATE.MESSAGE_DATA;
                        break;
                    case MESSAGE_DATA:
                        messageBuffer[messagePointer++] = datum;
                        if (messagePointer == messageSize) {
                            try {
                                String message = new String(messageBuffer, 0, messageSize);
                                receiver.sendCommand(message);
                                LOGGER.finer(new String(messageBuffer, 0, messageSize));
                            } catch (IOException e) {
                                LOGGER.log(Level.WARNING, "Error sending command to DCS-BIOS.", e);
                            }
                            state = CONTROLLER_STATE.WAITING;
                        }
                        break;
                }
            }
        }
    };

    public DcsBiosArduinoController(DcsBiosReceiver receiver, String serialPortName) {
        this.buffer = new ByteRingBuffer(4096);
        this.receiver = receiver;
        this.receiver.addStreamListener(this);
        setControllerReadyForData(false);
        state = CONTROLLER_STATE.WAITING;
        setSerialPortName(serialPortName);
    }

    public String getSerialPortName() {
        return serialPortName;
    }

    @SuppressWarnings("WeakerAccess")
    public synchronized void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.removeDataListener();
            serialPort.closePort();
            serialPort = null;
        }
        initSerialPort();
    }

    private void initSerialPort() {
        LOGGER.info(String.format("Opening serial port '%s'", serialPortName));
        serialPort = SerialPort.getCommPort(serialPortName);
        serialPort.openPort();
        serialPort.setComPortParameters(250000, 8, 1, 0);
        serialPort.addDataListener(listener);
        statusRequestPending = false;
        setControllerReadyForData(false);
        requestControllerStatus();
    }

    private synchronized void requestControllerStatus() {
        if (!statusRequestPending) {
            writeBuffer[0] = COMMAND_REQUEST_STATUS;
            serialPort.writeBytes(writeBuffer, 1);
            LOGGER.finer("Requesting controller status.");
            statusRequestPending = true;
        }
    }

    private synchronized boolean isControllerReadyForData() {
        return controllerReadyForData;
    }

    private synchronized void setControllerReadyForData(boolean value) {
        controllerReadyForData = value;
        sendBusExportStreamData();
    }

    /**
     * Note: Must be called from a synchronized method as it uses a class instance level write buffer.
     */
    private void sendBusExportStreamData() {
        if (isControllerReadyForData() && buffer.size() > 0) {
            int size = Math.min(buffer.size(), 64);
            writeBuffer[0] = COMMAND_LOAD_EXPORT_DATA;
            writeBuffer[1] = (byte)size;
            int checksum = size;
            for(int i=0;i<size;i++) {
                byte d = buffer.get();
                checksum += d;
                writeBuffer[2+i] = d;
            }
            writeBuffer[size+2] = (byte)checksum;
            serialPort.writeBytes(writeBuffer, size+3);
            setControllerReadyForData(false);
            LOGGER.finest(String.format("Sent %d bytes with %d remaining.", size, buffer.size()));
        }
    }

    @Override
    public synchronized void  dcsBiosStreamDataReceived(byte[] data, int offset, int length) {
        if (serialPort != null && serialPort.isOpen()) {
            buffer.add(data, offset, length);
            sendBusExportStreamData();
        }
    }

    private void processControllerNotification(byte data) {
        if (data == CONTROLLER_READY_FOR_DATA) {
            LOGGER.finer("Controller buffer ready.");
            statusRequestPending = false;
            setControllerReadyForData(true);
        } else if (data == CONTROLLER_BUFFER_FULL) {
            LOGGER.finer("Controller buffer full.");
            statusRequestPending = false;
            setControllerReadyForData(false);
        } else if (data == CONTROLLER_DATA_RECEIVED) {
            LOGGER.finer("Controller buffer data received.");
            // TODO Increment pointer.
        } else if (data == CONTROLLER_ERROR_LOADING) {
            LOGGER.warning("Error loading data to bus controller.");
            // TODO Rewind and retransmit.
        } else if (data == CONTROLLER_MESSAGE_RECEIVED) {
            messagePointer = 0;
            state = CONTROLLER_STATE.MESSAGE_SIZE;
        } else {
            LOGGER.warning(String.format("Unexpected data(%d) from bus controller.", data));
        }
    }
}