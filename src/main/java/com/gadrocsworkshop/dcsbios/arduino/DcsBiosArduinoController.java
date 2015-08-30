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
    private static final byte COMMAND_EXPORTSTREAM_DATA = (byte)'e';

    private static final byte CONTROLLER_READY_FOR_DATA = (byte)'r';
    private static final byte CONTROLLER_ERROR_LOADING = (byte)'x';
    private static final byte CONTROLLER_TRANSMITTING_PACKET = (byte)'t';
    private static final byte CONTROLLER_PACKET_RECEIVED = (byte)'m';

    private enum CONTROLLER_STATE {
        WAITING,
        MESSAGE_SIZE,
        MESSAGE_DATA
    }

    private final DcsBiosReceiver receiver;
    private final ByteRingBuffer buffer;

    private String serialPortName;
    private SerialPort serialPort;
    private boolean controllerReadyForData;
    private CONTROLLER_STATE state;
    private int messageSize;
    private int messagePointer;
    private byte[] messageBuffer = new byte[64];

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

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
        try {
            setControllerReadyForData(false);
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

    private SerialPort getSerialPort() {
        return serialPort;
    }

    private void initSerialPort() throws SerialPortException {
        LOGGER.info(String.format("Opening serial port '%s'", serialPortName));
        serialPort = new SerialPort(serialPortName);
        serialPort.openPort();
        serialPort.setParams(250000, 8, 1, 0);
        serialPort.addEventListener(this);
    }

    private void requestControllerStatus() {
        try {
            serialPort.writeByte(COMMAND_REQUEST_STATUS);
        } catch (SerialPortException e) {
            LOGGER.log(Level.WARNING, "Error requested controller status.", e);
        }
    }

    private boolean isControllerReadyForData() {
        return controllerReadyForData;
    }

    private void setControllerReadyForData(boolean value) {
        controllerReadyForData = value;
    }

    private void sendBusExportStreamData() {
        try {
            SerialPort port = getSerialPort();
            if (buffer.size() > 0) {
                int size = buffer.size() > 255 ? 255 : buffer.size();
                port.writeByte(COMMAND_EXPORTSTREAM_DATA);
                port.writeByte((byte) size);
                for(int i=0;i<size;i++) {
                    port.writeByte(buffer.get());
                }
                setControllerReadyForData(false);
                LOGGER.finest(String.format("Sent %d bytes with %d remaining.", size, buffer.size()));
            }
        } catch (SerialPortException ex) {
            LOGGER.log(Level.SEVERE, "Error sending data to bus controller.", ex);
        }
    }

    @Override
    public synchronized void  dcsBiosStreamDataReceived(byte[] data, int offset, int length) {
        buffer.add(data, offset, length);
        if (isControllerReadyForData()) {
            sendBusExportStreamData();
        } else if (serialPort != null && serialPort.isOpened()) {
            requestControllerStatus();
        }
    }

    private void processControllerNotification(byte data) {
        if (data == CONTROLLER_READY_FOR_DATA) {
            setControllerReadyForData(true);
            sendBusExportStreamData();
        } else if (data == CONTROLLER_TRANSMITTING_PACKET) {
            setControllerReadyForData(false);
        } else if (data == CONTROLLER_PACKET_RECEIVED) {
            messagePointer = 0;
            state = CONTROLLER_STATE.MESSAGE_SIZE;
        } else if (data == CONTROLLER_ERROR_LOADING) {
            LOGGER.warning("Error loading data to bus controller.");
            setControllerReadyForData(true);
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