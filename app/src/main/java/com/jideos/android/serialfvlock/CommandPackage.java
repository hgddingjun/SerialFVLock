package com.jideos.android.serialfvlock;

import android.util.Log;

import java.nio.IntBuffer;
import java.util.Arrays;

class CommandPackage {
    private final String TAG = "CommandPackage";
    static final byte CMD_DEV_OPEN            = 0x00;
    static final byte CMD_DEV_CLOSE           = 0x01;
    static final byte CMD_GET_BUTTON_STATE    = 0x03;
    static final byte CMD_ROLL_STEP           = 0x04;
    static final byte CMD_ROLL_STOP           = 0x05;
    static final byte CMD_IDENTY_USER         = 0x07;
    static final byte CMD_DEL_MODEL           = 0x09;
    static final byte CMD_DEL_ALL             = 0x0A;
    static final byte CMD_SET_TIMEOUT         = 0x0E;
    static final byte CMD_SET_BAUD_RATE       = 0x11;
    static final byte CMD_GET_ID              = 0x12;
    static final byte CMD_CANCEL_WAIT         = 0x13;
    static final byte CMD_SET_DEVICEID        = 0x17;
    static final byte CMD_GET_NEW_ID          = 0x1A;
    static final byte CMD_DEVICE_INIT         = 0x1C;
    static final byte CMD_GET_SYS_INFO        = 0x1D;
    static final byte CMD_GET_USER_INFO       = 0x1E;
    static final byte CMD_READ_START          = 0x1F;
    static final byte CMD_WRITE_START         = 0x20;
    static final byte CMD_READ_DATA           = 0x21;
    static final byte CMD_WRITE_DATA          = 0x22;
    static final byte CMD_READ_UNIQUE_ID      = 0x39;

    private static int mStart = 0xEF01;
    private static int mSequence = 0;

    private byte[] mBuf = new byte[32];
    private CommandMap mCommandMap;

    CommandPackage(byte command, byte[] param) {
        Log.d(TAG, "20190816-command: " + Integer.toHexString(command));
        switch (command) {
            case CMD_DEV_OPEN:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x00, param);
                break;
            case CMD_DEV_CLOSE:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x01, param);
                break;
            case CMD_GET_BUTTON_STATE:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x03, param);
                break;
            case CMD_ROLL_STEP:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x04, param);
                break;
            case CMD_ROLL_STOP:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x05, param);
                break;
            case CMD_IDENTY_USER:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x07, param);
                break;
            case CMD_DEL_MODEL:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x09, param);
                break;
            case CMD_DEL_ALL:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x0A, param);
                break;
            case CMD_SET_TIMEOUT:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x0E, param);
                break;
            case CMD_SET_BAUD_RATE:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x11, param);
                break;
            case CMD_GET_ID:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x12, param);
                break;
            case CMD_SET_DEVICEID:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x17, param);
                break;
            case CMD_GET_NEW_ID:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x1A, param);
                break;
            case CMD_DEVICE_INIT:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x1C, param);
                break;
            case CMD_GET_SYS_INFO:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x1D, param);
                break;
            case CMD_GET_USER_INFO:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x1E, param);
                break;
            case CMD_READ_UNIQUE_ID:
                generate(mStart, ++mSequence, 0xFFFF, 0x01, 0x000F, 0x39, param);
                break;
        }
        mCommandMap = new CommandMap(mSequence, command);
    }

    private void generate(int start, int sequence, int address, int type, int length, int command, byte[] param) {
        int crc16;

        mBuf[0]  = (byte) (start & 0xFF);
        mBuf[1]  = (byte) (start >> 8);
        mBuf[2]  = (byte) (sequence & 0xFF);
        mBuf[3]  = (byte) (sequence >> 8);
        mBuf[4]  = (byte) (address & 0xFF);
        mBuf[5]  = (byte) (address >> 8);
        mBuf[6]  = (byte) type;
        mBuf[7]  = (byte) (length & 0xFF);
        mBuf[8]  = (byte) (length >> 8);
        mBuf[9]  = (byte) command;
        if (param != null) {
            for (int i = 0; i < param.length; i++) {
                mBuf[10+i] = param[i];
            }
        }
        crc16 = (mBuf[6] & 0xFF) + (mBuf[7] & 0xFF) + (mBuf[8] & 0xFF);
        for (int i=0; i<length-2; i++) {
            crc16 += (mBuf[9+i] & 0xFF);
        }
        mBuf[22] = (byte) (crc16 & 0xFF);
        mBuf[23] = (byte) ((crc16 >> 8) & 0xFF);
    }

    byte[] toBytes() {
        return Arrays.copyOf(mBuf, 24);
    }

    CommandMap getCommandMap() {
        return mCommandMap;
    }
}
