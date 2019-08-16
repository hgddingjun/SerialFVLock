package com.jideos.android.serialfvlock;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

class FingerVein {
    private ICallback mICallback;
    private SerialPort mSerialPort;
    private RecThread mRecThread;

    void setCallback(ICallback callback) {
        mICallback = callback;
    }

    void open(String path, int rate) throws IOException {
        if (mSerialPort == null) {
            //mSerialPort = new SerialPort(path, rate);
            mSerialPort = new SerialPort(new File(path), rate, 0);
            mRecThread = new RecThread();
            mRecThread.start();
        }
    }

    void close() throws InterruptedException {
        if (mSerialPort != null) {
            mSerialPort.close();
            mRecThread.interrupt();
            mRecThread.join();
        }
    }

    void send(byte[] buf) {
        if (mSerialPort != null) {
            try {
                mSerialPort.getOutputStream().write(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class RecThread extends Thread {
        @Override
        public void run() {
            while (!interrupted()) {
                if (mSerialPort != null) {
                    try {
                        byte[] buf = new byte[4096];
                        int len = mSerialPort.getInputStream().read(buf);
                        if (len > 0) {
                            mICallback.onReceive(Arrays.copyOf(buf, len));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
        }
    }
}
