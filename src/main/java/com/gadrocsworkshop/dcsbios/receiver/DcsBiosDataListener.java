package com.gadrocsworkshop.dcsbios.receiver;

/**
 * Interface for objects which listen to data updates from the DCS-BIOS protocol.
 * All DcsBiosDataListener writes will be called from the same thread, but
 * will not be the same thread that created the object.  Data will not be in
 * a consistent state except during an DcsBiosSyncListener call.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
public interface DcsBiosDataListener {

    /**
     * Called when new data is available from the DCS-BIOS data stream.
     *
     * @param address Address into the aircraft data namespace for this data.
     * @param data Data element which has been updated.
     */
    void dcsBiosDataWriten(int address, int data);
}
