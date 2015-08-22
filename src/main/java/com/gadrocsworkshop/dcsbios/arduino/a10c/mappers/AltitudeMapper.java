package com.gadrocsworkshop.dcsbios.arduino.a10c.mappers;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoController;
import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoMapper;

public class AltitudeMapper extends DcsBiosArduinoMapper {

    private final int bank;
    private final int busIndex;

    private int tenThousands;
    private int thousands;
    private int hundreds;
    private int tens;

    public AltitudeMapper(DcsBiosArduinoController controller, int bank, int busIndex) {
        super(controller);
        this.bank = bank;
        this.busIndex = busIndex;
    }

    @Override
    public void dcsBiosDataWrite(int address, int data) {
        if (address == 0x107e) {
            tens = mapInteger(data, 0, 100);
        } else if (address == 0x1080) {
            tenThousands = mapInteger(data, 0, 10);
        } else if (address == 0x1082) {
            thousands = mapInteger(data, 0 ,10);
        } else if (address == 0x1084) {
            hundreds = mapInteger(data, 0, 10);
            long value = tenThousands*10000 + thousands*1000 + hundreds*100 + tens;
            getController().setShort(bank, busIndex, (short)value);
        }
    }
}
