package com.gadrocsworkshop.dcsbios;

import junit.framework.TestCase;

public class DcsBiosParserTest extends TestCase {

    private class CheckData {
        public int address;
        public int value;
        public boolean sync;

        public CheckData(int address, int value) {
            this.address = address;
            this.value = value;
            this.sync = false;
        }

        public CheckData() {
            this.sync = true;
        }
    }

    private class CheckDataListener implements DcsBiosDataListener, DcsBiosSyncListener {

        private CheckData[] expected;
        private int index = 0;
        private boolean valid = true;
        private boolean syncCalled = false;

        public CheckDataListener(CheckData[] expected) {
            this.expected = expected;
        }

        public boolean isValid() {
            return (index == expected.length) && valid;
        }

        @Override
        public void dcsBioDataWrite(int address, int data) {
            if (index < expected.length) {
                CheckData expectedValue = expected[index++];
                if (expectedValue.sync ||
                        Integer.compareUnsigned(expectedValue.address, address) != 0 ||
                        Integer.compareUnsigned(expectedValue.value, data) != 0) {
                    valid = false;
                }
            }
            else {
                valid = false;
            }
        }

        @Override
        public void handleDcsBiosFrameSync() {
            if (index < expected.length) {
                CheckData expectedValue = expected[index++];
                if (!expectedValue.sync) {
                    valid = false;
                }
            }
            else {
                valid = false;
            }
        }
    }

    private DcsBiosParser parser;

    public void setUp() throws Exception {
        super.setUp();
        parser = new DcsBiosParser();
    }

    public void tearDown() throws Exception {
        parser = null;
    }

    public void testParser() throws Exception {
        CheckData[] expected = {
                new CheckData(0x0000,0x0010),
                new CheckData(0x0004,0x1021),
                new CheckData(0x0006,0x3142),
                new CheckData(0x0002,0xff05),
                new CheckData(0x01ff,0x2475),
                new CheckData()
        };

        byte[] inputData = {
                0x55, 0x55, 0x55, 0x55,                         // Frame Start
                0x00, 0x00, 0x02, 0x00, 0x10, 0x00,             // One Integer (0x0010) at address 0x0000
                0x04, 0x00, 0x04, 0x00, 0x21, 0x10, 0x42, 0x31, // Two Integers (0x1021, 0x3142) at address 0x0004
                0x02, 0x00, 0x02, 0x00, 0x05, (byte)0xff,       // One Integer (0xff05) at address 0x0002
                (byte)0xff, 0x01, 0x02, 0x00, 0x75, 0x24,       // One Integer (0x2475) ad address 0x01ff
                (byte)0xfe, (byte)0xff, 0x02, 0x00, 0x00, 0x00  // End of frame
        };

        CheckDataListener listener = new CheckDataListener(expected);
        parser.addDataListener(listener);
        parser.addSyncListener(listener);
        parser.processData(inputData, 0, inputData.length);
        assertTrue("Expected output from listener not found.", listener.isValid());
        parser.removeDataListener(listener);
        parser.removeSyncListener(listener);
    }

    public void testMissingEndFrame() throws Exception {
        CheckData[] expected = {
                new CheckData(0x0000,0x0010),
                new CheckData(0x0004,0x1021),
                new CheckData(0x0006,0x5542),  // This is a bad situation  Protocol should handle it better.
                new CheckData(0x0002,0xff05),
                new CheckData(0x01ff,0x2475),
                new CheckData()
        };

        byte[] inputData = {
                0x55, 0x55, 0x55, 0x55,                         // Frame Start
                0x00, 0x00, 0x02, 0x00, 0x10, 0x00,             // One Integer (0x0010) at address 0x0000
                0x04, 0x00, 0x04, 0x00, 0x21, 0x10, 0x42,       // Two Integers (0x1021, 0x3142) at address 0x0004 (partial)
                0x55, 0x55, 0x55, 0x55,                         // Frame Start
                0x02, 0x00, 0x02, 0x00, 0x05, (byte)0xff,       // One Integer (0xff05) at address 0x0002
                (byte)0xff, 0x01, 0x02, 0x00, 0x75, 0x24,       // One Integer (0x2475) ad address 0x01ff
                (byte)0xfe, (byte)0xff, 0x02, 0x00, 0x00, 0x00  // End of frame
        };

        CheckDataListener listener = new CheckDataListener(expected);
        parser.addDataListener(listener);
        parser.addSyncListener(listener);
        parser.processData(inputData, 0, inputData.length);
        assertTrue("Expected output from listener not found.", listener.isValid());
        parser.removeDataListener(listener);
        parser.removeSyncListener(listener);
    }
}