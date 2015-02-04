package com.hsm;

public class ExitAction extends Action {

    protected State mPreviousState;

    public ExitAction previousState(State state) {
        mPreviousState = state;
        return this;
    }

}
