package com.gadrocsworkshop.dcsbios.arduino;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosSyncListener;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller which speaks with a controller using the DcsBiosArduino library.
 */
public class DcsBiosArduinoController implements DcsBiosSyncListener, SerialPortEventListener {

    private static final Logger LOGGER = Logger.getLogger(DcsBiosArduinoController.class.getName());

    private static final byte SEND_DATA_COMMAND = (byte)98;

    private final DcsBiosAdruinoPacketParser parser = new DcsBiosAdruinoPacketParser();
    private DcsBiosReceiver receiver;
    private String serialPortName;
    private SerialPort serialPort;

    private ByteBuffer data = ByteBuffer.allocate(124);
    private byte commandBuffer[] = new byte[62];

    public DcsBiosArduinoController(DcsBiosReceiver receiver, String serialPortName) {
        this.receiver = receiver;
        this.serialPortName = serialPortName;
        receiver.addSyncListener(this);
    }

    public String getSerialPortName() {
        return serialPortName;
    }

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
        if (serialPort != null && serialPort.isOpened()) {
            try {
                serialPort.removeEventListener();
                serialPort.closePort();
            } catch (SerialPortException ex) {
                LOGGER.log(Level.WARNING, "Error closing serial port on port name change.", ex);
            }
            serialPort = null;
        }
    }

    private SerialPort getSerialPort() throws SerialPortException{
        if (serialPort == null) {
            LOGGER.info(String.format("Opening serial port '%s'", serialPortName));
            serialPort = new SerialPort(serialPortName);
            serialPort.openPort();
            serialPort.setParams(250000, 8, 1, 0);
            serialPort.addEventListener(this);
        }
        return serialPort;
    }

    public void setBit(int bank, int index, byte mask, boolean value) {
        if (value) {
            setBit(bank, index, mask);
        } else {
            clearBit(bank, index, mask);
        }
    }

    public void setByte(int bank, int index, byte value) {
        int dataIndex = getDataIndex(bank, index);
        data.put(dataIndex, value);
    }

    public void setShort(int bank, int index, short value) {
        int dataIndex = getDataIndex(bank, index);
        data.putShort(dataIndex, value);
    }

    public void setInteger(int bank, int index, int value) {
        int dataIndex = getDataIndex(bank, index);
        data.putInt(dataIndex, value);
    }

    public void setBit(int bank, int index, byte mask) {
        int dataIndex = getDataIndex(bank, index);
        data.put(dataIndex, (byte) (data.get(dataIndex) | mask));
    }

    public void clearBit(int bank, int index, byte mask) {
        int dataIndex = getDataIndex(bank, index);
        data.put(dataIndex, (byte)(data.get(dataIndex) & ~mask));
    }

    private int getDataIndex(int bank, int index) {
        return bank * 31 + index;
    }

    @Override
    public void handleDcsBiosFrameSync() {
        try {
            SerialPort port = getSerialPort();
            port.writeByte(SEND_DATA_COMMAND);
            port.writeBytes(data.array());
        } catch (SerialPortException ex) {
            LOGGER.log(Level.SEVERE, "Error sending data to bus.", ex);
        }
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        if (serialPortEvent.isRXCHAR()) {
            try {
                int count = serialPortEvent.getEventValue();
                byte[] data = serialPort.readBytes(count);
                for (int i = 0; i < data.length; i++) {
                    DcsBiosArduinoCommandPacket packet = parser.processByte(data[i]);
                    if (packet != null) {
                        LOGGER.info(new String(packet.getCommandData(), "UTF-8"));
                        receiver.sendCommand(packet.getCommandData());
                    }
                }
            } catch (SerialPortException | IOException ex) {
                LOGGER.log(Level.WARNING, "Error reading from serial port.", ex);
            }
        }
    }

}
