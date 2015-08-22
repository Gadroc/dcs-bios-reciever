package com.gadrocsworkshop.dcsbios.arduino.mappers;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoController;
import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoMapper;

import java.util.logging.Logger;

public class FlagMapper extends DcsBiosArduinoMapper {
    private static final Logger LOGGER = Logger.getLogger(BitMapper.class.getName());

    private final int dcsBiosAddress;
    private final int dcsBiosThreshold;
    private final int bank;
    private final int busIndex;
    private final byte busMask;

    public FlagMapper(DcsBiosArduinoController controller, int dcsBiosAddress, int dcsBiosThreshold, int bank, int busIndex, int busBit) {
        super(controller);
        this.dcsBiosAddress = dcsBiosAddress;
        this.dcsBiosThreshold = dcsBiosThreshold;
        this.bank = bank;
        this.busIndex = busIndex;
        this.busMask = (byte)(1 << busBit);
    }

    @Override
    public void dcsBiosDataWrite(int address, int data) {
        if (address == dcsBiosAddress) {
            boolean value = data > dcsBiosThreshold;
            getController().setBit(bank, busIndex, busMask, value);
        }
    }
}
