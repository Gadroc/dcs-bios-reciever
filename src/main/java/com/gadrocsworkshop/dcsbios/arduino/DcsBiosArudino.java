package com.gadrocsworkshop.dcsbios.arduino;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosUdpReceiver;

/**
 * Created by Craig Courtney on 8/25/2015.
 */
public class DcsBiosArudino {
    public static void main(String[] args) {
        try {
            DcsBiosReceiver receiver = new DcsBiosUdpReceiver();
            receiver.start();

            DcsBiosArduinoController centerBus = new DcsBiosArduinoController(receiver, "COM7");

            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
