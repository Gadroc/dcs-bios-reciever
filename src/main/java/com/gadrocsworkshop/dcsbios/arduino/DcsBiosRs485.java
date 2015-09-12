package com.gadrocsworkshop.dcsbios.arduino;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosUdpReceiver;

public class DcsBiosRs485 {
    public static void main(String[] args) {
        try {
            DcsBiosReceiver receiver = new DcsBiosUdpReceiver();
            receiver.start();

            DcsBiosArduinoController bus = new DcsBiosArduinoController(receiver, args[0]);

            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
