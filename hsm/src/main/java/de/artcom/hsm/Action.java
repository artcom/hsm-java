package de.artcom.hsm;

import java.util.Map;

public abstract class Action {

    protected State mPreviousState;
    protected State mNextState;
    protected Map<String, Object> mPayload;

    public abstract void run();

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
