package com.hsm;

import java.util.Map;

public abstract class Action {

    private State mPreviousState = null;
    private State mNextState = null;
    private Map<String, Object> mPayload = null;

    public abstract void run();

    public Map<String, Object> getPayload() {
        return mPayload;
    }

    //public State getPreviousState()
    //public State getNextState()

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
