package com.hsm;

public class Action implements Runnable {

    protected State mPreviousState = null;
    protected State mNextState = null;

    @Override
    public void run() {

    }

    public Action previousState(State state) {
        mPreviousState = state;
        return this;
    }

    public Action nextState(State state) {
        mNextState = state;
        return this;
    }
}
