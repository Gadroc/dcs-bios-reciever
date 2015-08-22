package com.gadrocsworkshop.dcsbios.arduino.a10c;

import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosUdpReceiver;

public class A10C {

    public static void main(String[] args) {
        try {
            DcsBiosReceiver receiver = new DcsBiosUdpReceiver();
            receiver.start();

            CenterConsoleBus centerBus = new CenterConsoleBus(receiver, "COM7");

            while(true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
