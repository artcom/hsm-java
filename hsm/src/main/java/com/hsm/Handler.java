package com.hsm;

class Handler {

    private final String mTargetStateId;
    private final TransitionType mType;
    private Action mAction = null;

    public Handler(String targetStateId, TransitionType type, Action action) {
        mTargetStateId = targetStateId;
        mType = type;
        mAction = action;
    }

    public Handler(String targetStateId, TransitionType type) {
        mTargetStateId = targetStateId;
        mType = type;
    }

    public boolean isQualifiedToHandle(Event event) {
        //TODO: implement guard
        return true;
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
