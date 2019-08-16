package com.jideos.android.serialfvlock;

import java.util.Arrays;

class ResponsePackage {
    private int mSequence;
    private int mResult;

    private byte[] mBuf;

    ResponsePackage(byte[] buf) {
        mSequence = ((buf[3] & 0xFF) << 8) | (buf[2] & 0xFF);
        mResult = (buf[9] & 0xFF);

        mBuf = Arrays.copyOf(buf, buf.length);
    }

    public int getSequence() {
        return mSequence;
    }

    int getResult() {
        return mResult;
    }

    byte getByte(int where) {
        return mBuf[where];
    }
}
