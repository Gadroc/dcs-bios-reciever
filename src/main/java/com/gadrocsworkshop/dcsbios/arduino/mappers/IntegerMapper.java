package com.gadrocsworkshop.dcsbios.arduino.mappers;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoController;
import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoMapper;

public class IntegerMapper extends DcsBiosArduinoMapper {

    private final int dcsBiosAddress;
    private final int bank;
    private final int busIndex;

    public IntegerMapper(DcsBiosArduinoController controller, int dcsBiosAddress, int bank, int busIndex) {
        super(controller);
        this.dcsBiosAddress = dcsBiosAddress;
        this.bank = bank;
        this.busIndex = busIndex;
    }

    @Override
    public void dcsBiosDataWrite(int address, int data) {
        if (address == dcsBiosAddress) {
            getController().setShort(bank, busIndex, (short)data);
        }
    }

}
