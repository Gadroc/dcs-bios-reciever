package com.gadrocsworkshop.dcsbios.arduino.a10c.mappers;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoController;
import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoMapper;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ClockIndicatorMapper extends DcsBiosArduinoMapper {

    private static final Logger LOGGER = Logger.getLogger(ClockMapper.class.getName());

    private final int bank;
    private final int busIndex;
    private final byte etMask;
    private final byte cMask;
    private final byte clearMask;

    private int firstInteger;

    public ClockIndicatorMapper(DcsBiosArduinoController controller, int bank, int busIndex, int busBitC, int busBitEt) {
        super(controller);
        this.bank = bank;
        this.busIndex = busIndex;
        this.cMask = (byte) (1 << busBitC);
        this.etMask = (byte) (1 << busBitEt);
        this.clearMask = (byte)(this.cMask | this.etMask);
    }

    @Override
    public void dcsBiosDataWrite(int address, int data) {
        if (address == 0x1104) {
            firstInteger = data;
        } else if (address == 0x1106) {
            try {
                byte[] bytes = {(byte)(firstInteger >> 8), (byte)(firstInteger & 0xFF), (byte)(data >>8)};
                String stringData = new String(bytes, "UTF-8");
                getController().clearBit(bank, busIndex, clearMask);
                if (stringData.equals("ET")) {
                    getController().setBit(bank, busIndex, etMask);
                } else if (stringData.equals("C")) {
                    getController().setBit(bank, busIndex, cMask);
                }
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Error converting clock string value into a bits.", ex);
            }
        }
    }
}