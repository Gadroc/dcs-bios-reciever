package com.gadrocsworkshop.dcsbios.arduino;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosStreamListener;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller which speaks with a controller using the DcsBiosArduino library.
 */
public class DcsBiosArduinoController implements DcsBiosStreamListener, SerialPortEventListener {

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

    private boolean statusRequestPending;
    private boolean controllerReadyForData;
    private CONTROLLER_STATE state;

    private int messageSize;
    private int messagePointer;
    private final byte[] messageBuffer = new byte[64];

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
        try {
            if (serialPort != null && serialPort.isOpened()) {
                serialPort.removeEventListener();
                serialPort.closePort();
                serialPort = null;
            }
            initSerialPort();
        } catch (SerialPortException ex) {
            LOGGER.log(Level.WARNING, "Error changing serial port.", ex);
        }
    }

    private void initSerialPort() throws SerialPortException {
        LOGGER.info(String.format("Opening serial port '%s'", serialPortName));
        serialPort = new SerialPort(serialPortName);
        serialPort.openPort();
        serialPort.setParams(250000, 8, 1, 0);
        serialPort.addEventListener(this);
        statusRequestPending = false;
        setControllerReadyForData(false);
        requestControllerStatus();
    }

    private synchronized void requestControllerStatus() {
        try {
            if (!statusRequestPending) {
                serialPort.writeByte(COMMAND_REQUEST_STATUS);
                LOGGER.finer("Requesting controller status.");
                statusRequestPending = true;
            }
        } catch (SerialPortException e) {
            LOGGER.log(Level.WARNING, "Error requested controller status.", e);
        }
    }

    private synchronized boolean isControllerReadyForData() {
        return controllerReadyForData;
    }

    private synchronized void setControllerReadyForData(boolean value) {
        controllerReadyForData = value;
        sendBusExportStreamData();
    }

    private void sendBusExportStreamData() {
        try {
            if (isControllerReadyForData() && buffer.size() > 0) {
                int size = buffer.size() > 64 ? 64 : buffer.size();
                serialPort.writeByte(COMMAND_LOAD_EXPORT_DATA);
                serialPort.writeByte((byte) size);
                int checksum = size;
                for(int i=0;i<size;i++) {
                    byte d = buffer.get();
                    checksum += d;
                    serialPort.writeByte(d);
                }
                serialPort.writeByte((byte)checksum);
                setControllerReadyForData(false);
                LOGGER.finest(String.format("Sent %d bytes with %d remaining.", size, buffer.size()));
            }
        } catch (SerialPortException ex) {
            LOGGER.log(Level.SEVERE, "Error sending data to bus controller.", ex);
        }
    }

    @Override
    public synchronized void  dcsBiosStreamDataReceived(byte[] data, int offset, int length) {
        if (serialPort != null && serialPort.isOpened()) {
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

    @Override
    public synchronized void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            try {
                int count = serialPortEvent.getEventValue();
                byte[] data = serialPort.readBytes(count);
                for (int i = 0; i < count; i++) {
                    switch (state) {
                        case WAITING:
                            processControllerNotification(data[i]);
                            break;
                        case MESSAGE_SIZE:
                            messageSize = data[i];
                            state = CONTROLLER_STATE.MESSAGE_DATA;
                            break;
                        case MESSAGE_DATA:
                            messageBuffer[messagePointer++] = data[i];
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
            } catch (SerialPortException ex) {
                LOGGER.log(Level.WARNING, "Error reading from bus controller.", ex);
            }
        }
    }
}