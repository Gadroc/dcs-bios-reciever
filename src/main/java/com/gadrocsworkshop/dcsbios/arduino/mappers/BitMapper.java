package com.gadrocsworkshop.dcsbios.arduino.mappers;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoController;
import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoMapper;

import java.util.logging.Logger;

public class BitMapper extends DcsBiosArduinoMapper {
    private static final Logger LOGGER = Logger.getLogger(BitMapper.class.getName());

    private final int dcsBiosAddress;
    private final int dcsBiosMask;
    private final int bank;
    private final int busIndex;
    private final byte busMask;

    public BitMapper(DcsBiosArduinoController controller, int dcsBiosAddress, int dcsBiosMask, int bank, int busIndex, int busBit) {
        super(controller);
        this.dcsBiosAddress = dcsBiosAddress;
        this.dcsBiosMask = dcsBiosMask;
        this.bank = bank;
        this.busIndex = busIndex;
        this.busMask = (byte)(1 << busBit);
    }

    @Override
    public void dcsBiosDataWrite(int address, int data) {
        if (address == dcsBiosAddress) {
            boolean value = ((data & dcsBiosMask) == dcsBiosMask);
            getController().setBit(bank, busIndex, busMask, value);
        }
    }
}
