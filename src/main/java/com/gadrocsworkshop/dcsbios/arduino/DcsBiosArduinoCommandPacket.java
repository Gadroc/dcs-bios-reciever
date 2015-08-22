package com.gadrocsworkshop.dcsbios.arduino;

public class DcsBiosArduinoCommandPacket {

    private final int sourceDeviceAddress;
    private final byte[] commandData;

    public DcsBiosArduinoCommandPacket(int sourceDeviceAddress, byte[] commandData) {
        this.sourceDeviceAddress = sourceDeviceAddress;
        this.commandData = commandData;
    }

    public int getSourceDeviceAddress() {
        return sourceDeviceAddress;
    }

    public byte[] getCommandData() {
        return commandData;
    }
}
