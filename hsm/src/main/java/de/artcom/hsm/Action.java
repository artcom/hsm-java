package de.artcom.hsm;

import java.util.Map;

public abstract class Action {

    protected State mPreviousState = null;

    protected State mNextState = null;

    protected Map<String, Object> mPayload = null;

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
