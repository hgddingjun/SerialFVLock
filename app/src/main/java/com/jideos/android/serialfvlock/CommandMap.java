package com.jideos.android.serialfvlock;

class CommandMap {
    private int mSequence;
    private byte mCommand;

    public CommandMap(int sequence, byte command) {
        mSequence = sequence;
        mCommand = command;
    }

    public int getSequence() {
        return mSequence;
    }

    public byte getCommand() {
        return mCommand;
    }
}
