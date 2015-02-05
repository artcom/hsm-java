package com.hsm;

class Transition {

    private final String mTargetStateId;
    private final TransitionType mType;
    private Action mAction = null;

    public Transition(String targetStateId, TransitionType type, Action action) {
        mTargetStateId = targetStateId;
        mType = type;
        mAction = action;
    }

    public Transition(String targetStateId, TransitionType type) {
        mTargetStateId = targetStateId;
        mType = type;
    }

    public String getTargetStateId() {
        return mTargetStateId;
    }

    public Action getAction() {
        return mAction;
    }

    public TransitionType getType() {
        return mType;
    }

}
