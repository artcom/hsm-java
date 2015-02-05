package com.hsm;

class Transition {

    private String mTargetStateId;
    private Action mAction;

    public Transition(String targetStateId, Action action) {
        mTargetStateId = targetStateId;
        mAction = action;
    }

    public Transition(String targetStateId) {
        mTargetStateId = targetStateId;
    }

    public String getTargetStateId() {
        return mTargetStateId;
    }

    public Action getAction() {
        return mAction;
    }

}
