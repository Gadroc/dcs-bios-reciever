package com.gadrocsworkshop.dcsbios.arduino;

public class DcsBiosAdruinoPacketParser {

    private static final byte DCSBIOS_PACKET_START_BYTE = (byte)0xBB;
    private static final byte DCSBIOS_PACKET_LEADIN_BYTE = (byte)0x88;

    private enum PARSER_STATE {
        START,
        LEADIN,
        ADDRESS,
        SIZE,
        DATA,
        CHECKSUM
    }

    private PARSER_STATE state;
    private int type;
    private int address;
    private int index;
    private byte size;
    private byte[] buffer = new byte[64];
    private byte checksum;

    public DcsBiosAdruinoPacketParser() {
        reset();
    }

    public DcsBiosArduinoCommandPacket processByte(byte in) {

        DcsBiosArduinoCommandPacket packet = null;

        switch (state) {
            case START:
                if (in == DCSBIOS_PACKET_START_BYTE) {
                    state = PARSER_STATE.LEADIN;
                }
                break;
            case LEADIN:
                if (in == DCSBIOS_PACKET_LEADIN_BYTE) {
                    state = PARSER_STATE.ADDRESS;
                } else {
                    reset();
                }
                break;
            case ADDRESS:
                type = (in & 0xE0) >> 5;
                address = in & 0x1F;
                checksum = in;
                state = PARSER_STATE.SIZE;
                break;
            case SIZE:
                size = in;
                checksum += size;
                if (size > 0) {
                    state = PARSER_STATE.DATA;
                } else {
                    state = PARSER_STATE.CHECKSUM;
                }
                break;
            case DATA:
                checksum += in;
                buffer[index++] = in;
                if (index == size) {
                    state = PARSER_STATE.CHECKSUM;
                }
                break;
            case CHECKSUM:
                if (in == checksum) {
                    byte[] packetData = new byte[size];
                    for (int i=0; i<size; i++) {
                        packetData[i] = buffer[i];
                    }
                    packet = new DcsBiosArduinoCommandPacket(address, packetData);
                }
                reset();
                break;
        }

        return packet;
    }

    private void reset() {
        state = PARSER_STATE.START;
        index = 0;
        size = 0;
        checksum = 0;
    }

}
