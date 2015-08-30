package com.gadrocsworkshop.dcsbios.receiver;

/**
 * Interface for objects which need to process / display data from the DCS-BIOS data
 * stream.  Objects which read from the DcsBiosSyncListener streams can only be
 * validly read inside calls to dcsBiosFrameSyncReceived.  If read outside this method
 * they may be in an inconsistent or partially updated state.
 *
 * Created by Craig Courtney on 1/30/2015.
 */
public interface DcsBiosSyncListener {
    /**
     * Called at the end of a frame data.  All IDcsBiosDataListeners can
     * be assumed to be consistent during the length of this call and can
     * be referenced.
     */
    void dcsBiosFrameSyncReceived();
}
