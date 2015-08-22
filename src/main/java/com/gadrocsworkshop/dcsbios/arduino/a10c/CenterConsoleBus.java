package com.gadrocsworkshop.dcsbios.arduino.a10c;

import com.gadrocsworkshop.dcsbios.arduino.DcsBiosArduinoConsoleBus;
import com.gadrocsworkshop.dcsbios.arduino.a10c.mappers.AltitudeMapper;
import com.gadrocsworkshop.dcsbios.arduino.a10c.mappers.ClockIndicatorMapper;
import com.gadrocsworkshop.dcsbios.arduino.a10c.mappers.ClockMapper;
import com.gadrocsworkshop.dcsbios.receiver.DcsBiosReceiver;

public class CenterConsoleBus extends DcsBiosArduinoConsoleBus {

    public CenterConsoleBus(DcsBiosReceiver receiver, String serialPortName) {
        super(receiver, serialPortName);

        // Flight Instrument Light Level
        createIntegerMapper(0x114a, 0, 0);
        // ADI Glide Slope Indicator Position
        createIntegerMapper(0x1044, 0, 2);
        // ADI Slipball Position
        createIntegerMapper(0x1036, 0, 4);
        // ADI Bank Steering Bar Position
        createIntegerMapper(0x1040, 0, 6);
        // ADI Pitch Steering Bar Position
        createIntegerMapper(0x1042, 0, 8);
        // ADI Turn Needle Position
        createIntegerMapper(0x1038, 0, 10);
        // ADI Indicated Airspeed (Represents 0-550 knots)
        createIntegerMapper(0x107a, 0, 12);
        // SAI Pitch
        createIntegerMapper(0x102a, 0, 14);
        // SAI Bank
        createIntegerMapper(0x1028, 0, 16);
        // Angle of Attack
        createIntegerMapper(0x1078, 0, 18);
        // Vertical Velocity (-6000 to 6000)
        createIntegerMapper(0x106e, 0, 20);
        // Altitude
        addMapper(new AltitudeMapper(getArduinoController(), 0, 22));
        // Clock Hours
        addMapper(new ClockMapper(getArduinoController(), 0x10fe, 0, 26));
        // Clock Minutes
        addMapper(new ClockMapper(getArduinoController(), 0x1100, 0, 27));
        // Clock Seconds
        addMapper(new ClockMapper(getArduinoController(), 0x1102, 0, 28));
        // ADI Attitude(Off) Flag
        createFlagMapper(0x103a, 0, 29, 0);
        // ADI Course Warning Flag
        createFlagMapper(0x103c, 0, 29, 1);
        // ADI Glide Warning Flag
        createFlagMapper(0x103e, 0 ,29, 2);
        // SAI Off Warning Flag
        createFlagMapper(0x102c, 0, 29, 3);
        // AoA Off Warning Flag
        createFlagMapper(0x1076, 0, 29, 4);
        // NMSP Hars Button LED
        createBitMapper(0x1110, 0x0200, 0, 29, 5);
        // NMSP EGI Button LED
        createBitMapper(0x1110, 0x0800, 0, 29, 6);
        // NMSP TISL Button LED
        createBitMapper(0x1110, 0x2000, 0, 29, 7);
        // NMSP STR PT Button LED
        createBitMapper(0x1110, 0x8000, 0, 30, 0);
        // NMSP ANCHR Button LED
        createBitMapper(0x1112, 0x0002, 0, 30, 1);
        // NMSP TCN Button LED
        createBitMapper(0x1112, 0x0004, 0, 30, 2);
        // NMSP ILS Button LED
        createBitMapper(0x1112, 0x0020, 0, 30, 3);
        // NMSP UHF Led
        createBitMapper(0x11bc, 0x0002, 0, 30, 4);
        // NMSP VHF Led
        createBitMapper(0x11bc, 0x0004, 0, 30, 5);
        // Clock C Mode Indicator
        addMapper(new ClockIndicatorMapper(getArduinoController(), 0, 30, 6, 7));

        // Aux Instruments Light Level
        createIntegerMapper(0x114c, 2, 0);
        // Left Hydraulic Pressure (4)
        // Right Hydraulic Pressure (6)
        // Left Fuel Needle (7)
        // Right Fuel Needle (8)
        // Flaps Position Indicator
        createIntegerMapper(0x10a0, 2, 10);
        // Accelerometer G Load Needle (12)
        // Accelerometer Min G Load Needle (14)
        // Accelerometer Max G Load Needle (16)
        // Standby Compass Heading (18)
        // UHF Frequency Repeater (20)
        // Gun Ready Indicator (26,0)
        // Steering Engaged Indicator (26, 1)
        // Left Engine Fire Indicator (26, 2)
        // APU Fire Indicator (26, 3)
        // Right Engine Fire Indicator (26, 4)
        // Refuel Indexer Ready Indicator (26, 5)
        // Refuel Indexer Latched Indicator (26, 6)
        // Refuel Indexer Disconnect Indicator (26, 7)
        // AoA Indexer Low Speed Indicator (27, 0)
        // AoA Indexer On Speed Indicator (27, 1)
        // AoA Indexer High Spped Indicator (27, 2)
        // TISL TISL Indicator (27, 3)
        // TISL Over Temp Indicator (27, 4)
        // TISL Track Indicator (27, 5)
        // TISL DET ACD Indicator (27, 6)
        // Compass & Accelerometer Lights (27, 7)
        // Landing Gear Safe Indicators
        createBitMapper(0x1026, 0x1000, 2, 28, 0);
        createBitMapper(0x1026, 0x0800, 2, 28, 1);
        createBitMapper(0x1026, 0x2000, 2, 28, 2);
        // Landing Gear Handle Light
        createBitMapper(0x1026, 0x4000, 2, 28, 3);
        // Marker Beacon Indicator (28, 4)
        // Canopy Unlocked Indicator (28, 5)
    }
}
