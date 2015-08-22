package com.gadrocsworkshop.dcsbios.arduino;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosDataListener;

/**
 * Base class for implementing mappers which are used to translate DcsBios memory format to new address in
 * the arduino controller address space.
 */
public abstract class DcsBiosArduinoMapper implements DcsBiosDataListener {

    private final DcsBiosArduinoController controller;

    public DcsBiosArduinoMapper(DcsBiosArduinoController controller) {
        this.controller = controller;
    }

    public DcsBiosArduinoController getController() {
        return controller;
    }

    protected  int mapInteger(int value, int targetLow, int targetHigh) {
        return mapInteger(value, 0, 65535, targetLow, targetHigh);
    }

    protected int mapInteger(int value, int sourceLow, int sourceHigh, int targetLow, int targetHigh) {
        return (int)Math.floor((value-sourceLow)/(sourceHigh-sourceLow)*(targetHigh-targetLow)+targetLow);
    }
}
