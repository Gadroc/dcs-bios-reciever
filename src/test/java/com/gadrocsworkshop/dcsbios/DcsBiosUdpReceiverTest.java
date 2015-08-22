package com.gadrocsworkshop.dcsbios;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosUdpReceiver;
import junit.framework.TestCase;

public class DcsBiosUdpReceiverTest extends TestCase {

    public void testThread() throws Exception {
        DcsBiosUdpReceiver receiver =  new DcsBiosUdpReceiver();
        assertFalse("isRunning should return false after initialization.", receiver.isRunning());

        receiver.start();
        Thread.sleep(1000);
        assertTrue("isRunning should return true after start.", receiver.isRunning());

        Thread.sleep(2000);

        receiver.stop();
        Thread.sleep(2000);
        assertFalse("isRunning should return false after stop.", receiver.isRunning());
    }
}