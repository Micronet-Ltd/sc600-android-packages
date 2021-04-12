
package com.swfp.utils;

public class MessageType {
    public static final int FP_MSG_ALL_MSGS = 65535;
    public static final int FP_MSG_ENROLL = 16;
    public static final int FP_MSG_ENROLL_COMPLETE = 2;
    public static final int FP_MSG_ENROLL_PERCENT = 1;
    public static final int FP_MSG_ERROR = 1;
    public static final int FP_MSG_FINGER = 32;
    public static final int FP_MSG_FINGER_LEAVE = 4;
    public static final int FP_MSG_FINGER_TOUCH = 2;
    public static final int FP_MSG_FINGER_WAIT_LEAVE = 3;
    public static final int FP_MSG_FINGER_WAIT_TOUCH = 1;
    public static final int FP_MSG_IMG = 4;
    public static final int FP_MSG_TEST = 4096;
    public static final int FP_MSG_TEST_CMD_CALIBRATION_CLEAR = 130;
    public static final int FP_MSG_TEST_CMD_CALIBRATION_KVALUE = 132;
    public static final int FP_MSG_TEST_CMD_CALIBRATION_STATUS = 131;
    public static final int FP_MSG_TEST_CMD_CALIBRATION_WRITE = 129;
    public static final int FP_MSG_TEST_CMD_CANCEL = 291;
    public static final int FP_MSG_TEST_CMD_IC_SIZE = 32;
    public static final int FP_MSG_TEST_CMD_IRQ = 148;
    public static final int FP_MSG_TEST_CMD_LIB_VER = 33;
    public static final int FP_MSG_TEST_CMD_PIXEL = 149;
    public static final int FP_MSG_TEST_CMD_PIXEL_IMG = 146;
    public static final int FP_MSG_TEST_CMD_PIXEL_NUM = 145;
    public static final int FP_MSG_TEST_CMD_QUERY_FINGER = 294;
    public static final int FP_MSG_TEST_CMD_REMOVE_FINGER = 292;
    public static final int FP_MSG_TEST_CMD_SIMULATION_IMG = 293;
    public static final int FP_MSG_TEST_CMD_SPI_RDWR = 147;
    public static final int FP_MSG_TEST_CMD_START_AUTH = 290;
    public static final int FP_MSG_TEST_CMD_START_ENROLL = 289;
    public static final int FP_MSG_TEST_CMD_START_TEST = 288;
    public static final int FP_MSG_TEST_CMD_WRITE_K = 4096;
    public static final int FP_MSG_TEST_IMG_QUALITY = 150;
    public static final int FP_MSG_TEST_READ_AGC = 154;
    public static final int FP_MSG_TEST_READ_BIN = 153;
    public static final int FP_MSG_TEST_READ_IMG = 151;
    public static final int FP_MSG_TEST_RESULT_ENROLL = 512;
    public static final int FP_MSG_TEST_RESULT_ERROR = 128;
    public static final int FP_MSG_TEST_RESULT_OK = 1;
    public static final int FP_MSG_TEST_RESULT_ON_ACQUIRED = 513;
    public static final int FP_MSG_TEST_RESULT_ON_AUTHENTICATED = 514;
    public static final int FP_MSG_TEST_RESULT_ON_ENUMERATE = 517;
    public static final int FP_MSG_TEST_RESULT_ON_ERROR = 515;
    public static final int FP_MSG_TEST_RESULT_ON_REMOVED = 516;
    public static final int FP_MSG_TEST_VALID_PIXNUM_AND_QUALITY = 152;
    public static final int FP_MSG_TOUCH = 2;
    public static final int FP_MSG_VERIFY = 8;
    public static final int FP_MSG_VERIFY_NOT_MACH = 2;
    public static final int FP_MSG_VERIFY_OK = 1;

    public MessageType() {
        super();
    }

    public static String getEnrollStatue(int arg1) {
        switch(arg1) {
            case 1: {
                return "ENROLL_PERCENT";
            }
            case 2: {
                return "ENROLL_COMPLETE";
            }
        }

        return "UNKNOWN ENROLL : " + arg1;
    }

    public static String getFingerStatue(int arg1) {
        switch(arg1) {
            case 1: {
                return "wait finger touch";
            }
            case 2: {
                return "finger is touch";
            }
            case 3: {
                return "wait finger leave";
            }
            case 4: {
                return "finger is leave";
            }
        }

        return "UNKNOWN Finger : " + arg1;
    }

    public static String getMegInfo(int what, int arg1, int arg2) {
        String v0 = "msg:" + MessageType.getMsgWhat(what);
        switch(what) {
            case 8: {
                v0 += MessageType.getVerifyStatue(arg1);
                break;
            }
            case 16: {
                v0 += MessageType.getEnrollStatue(arg1);
                break;
            }
            case 32: {
                v0 += MessageType.getFingerStatue(arg1);
                break;
            }
        }

        return v0;
    }

    public static String getMsgWhat(int msg) {
        switch(msg) {
            case 1: {
                return "ERROR";
            }
            case 2: {
                return "TOUCH";
            }
            case 4: {
                return "Image ready";
            }
            case 8: {
                return "Verify:";
            }
            case 16: {
                return "Enroll:";
            }
            case 32: {
                return "Finger:";
            }
            case 288: {
                return "setmode";
            }
            case 289: {
                return "enroll";
            }
            case 290: {
                return "auth";
            }
            case 291: {
                return "cancel";
            }
            case 292: {
                return "remove";
            }
            case 293: {
                return "sendimage";
            }
            case 294: {
                return "query";
            }
            case 512: {
                return "onEnroll";
            }
            case 513: {
                return "onAcquired";
            }
            case 514: {
                return "onAuth";
            }
            case 515: {
                return "onError";
            }
            case 516: {
                return "onRemove";
            }
            case 517: {
                return "onEnumerate";
            }
            case 65535: {
                return "All:";
            }
        }

        return "UNKNOWN Message : " + msg;
    }

    public static String getVerifyStatue(int arg1) {
        switch(arg1) {
            case 1: {
                return "verify match";
            }
            case 2: {
                return "verify not match";
            }
        }

        return "UNKNOWN verify: " + arg1;
    }
}

