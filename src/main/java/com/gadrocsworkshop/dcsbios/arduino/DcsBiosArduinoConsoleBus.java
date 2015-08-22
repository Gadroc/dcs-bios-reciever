package com.gadrocsworkshop.dcsbios.arduino;

import com.gadrocsworkshop.dcsbios.arduino.mappers.BitMapper;
import com.gadrocsworkshop.dcsbios.arduino.mappers.FlagMapper;
import com.gadrocsworkshop.dcsbios.arduino.mappers.IntegerMapper;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;

import java.util.ArrayList;
import java.util.List;

public abstract class DcsBiosArduinoConsoleBus {

    private final DcsBiosReceiver receiver;
    private final DcsBiosArduinoController arduinoController;
    private final List<DcsBiosArduinoMapper> mappers = new ArrayList<>();

    public DcsBiosArduinoConsoleBus(DcsBiosReceiver receiver, String serialPortName) {
        this.receiver = receiver;
        arduinoController = new DcsBiosArduinoController(receiver, serialPortName);
    }

    public DcsBiosArduinoController getArduinoController() {
        return arduinoController;
    }

    protected void createBitMapper(int dcsBiosAddress, int dcsBiosMask, int bank, int busIndex, int busBit) {
        addMapper(new BitMapper(getArduinoController(), dcsBiosAddress, dcsBiosMask, bank, busIndex, busBit));
    }

    protected void createIntegerMapper(int dcsBiosAddress, int bank, int busIndex) {
        addMapper(new IntegerMapper(getArduinoController(), dcsBiosAddress, bank, busIndex));
    }

    protected void createFlagMapper(int dcsBiosAddress, int bank, int busIndex, int busBit) {
        createFlagMapper(dcsBiosAddress, 0x0100, bank, busIndex, busBit);
    }

    protected void createFlagMapper(int dcsBiosAddress, int dcsBiosThreshold, int bank, int busIndex, int busBit) {
        addMapper(new FlagMapper(getArduinoController(), dcsBiosAddress, dcsBiosThreshold, bank, busIndex, busBit));
    }

    protected void addMapper(DcsBiosArduinoMapper mapper) {
        mappers.add(mapper);
        receiver.addDataListener(mapper);
    }

}
