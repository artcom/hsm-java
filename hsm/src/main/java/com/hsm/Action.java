package com.hsm;

import java.util.Map;

public class Action implements Runnable {

    protected State mPreviousState = null;
    protected State mNextState = null;
    protected Map<String, Object> mPayload = null;

    @Override
    public void run() {
    }

    public Map<String, Object> getPayload() {
        return mPayload;
    }

    void setPreviousState(State state) {
        mPreviousState = state;
    }

    void setNextState(State state) {
        mNextState = state;
    }

    void setPayload(Map<String, Object> payload) {
        mPayload = payload;
    }
}
