package com.gadrocsworkshop.dcsbios.arduino.a10c.mappers;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoController;
import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClockMapper extends DcsBiosArduinoMapper {

    private static final Logger LOGGER = Logger.getLogger(ClockMapper.class.getName());

    private final int dcsBiosAddress;
    private final int bank;
    private final int busIndex;

    public ClockMapper(DcsBiosArduinoController controller, int dcsBiosAddress, int bank, int busIndex) {
        super(controller);
        this.dcsBiosAddress = dcsBiosAddress;
        this.bank = bank;
        this.busIndex = busIndex;
    }

    @Override
    public void dcsBiosDataWrite(int address, int data) {
        if (address == dcsBiosAddress) {
            byte value = (byte)0xFF;
            try {
                byte[] bytes = {(byte) (data >> 8), (byte) (data & 0xFF)};
                String stringData = new String(bytes, "UTF-8");
                if (!stringData.isEmpty()) {
                    value = Byte.parseByte(stringData);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error converting clock string value into a byte.", ex);
            }
            getController().setByte(bank, busIndex, value);
        }
    }
}
