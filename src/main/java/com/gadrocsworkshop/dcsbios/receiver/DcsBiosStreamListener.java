package com.gadrocsworkshop.dcsbios.receiver;

import java.nio.ByteBuffer;

/**
 * Interface for objects which listen to the raw stream from the DCS-BIOS protocol.
 * All DcsBiosPacketListener calls will be from the same thread, but
 * will not be the same thread that created the object.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
public interface DcsBiosStreamListener {
    void dcsBiosStreamDataReceived(byte[] data, int offset, int length);
}
