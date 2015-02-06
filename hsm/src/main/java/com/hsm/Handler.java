package com.hsm;

class Handler {

    private final String mTargetStateId;
    private final TransitionType mType;
    private Guard mGuard = null;
    private Action mAction = null;

    public Handler(String targetStateId, TransitionType type, Action action, Guard guard) {
        mTargetStateId = targetStateId;
        mType = type;
        mAction = action;
        mGuard = guard;
    }

    public Handler(String targetStateId, TransitionType type, Guard guard) {
        mTargetStateId = targetStateId;
        mType = type;
        mGuard = guard;
    }

    public Handler(String targetStateId, TransitionType type, Action action) {
        mTargetStateId = targetStateId;
        mType = type;
        mAction = action;
    }

    public Handler(String targetStateId, TransitionType type) {
        mTargetStateId = targetStateId;
        mType = type;
    }

    public boolean evaluate(Event event) {
        if(mGuard != null) {
            return mGuard.evaluate(event.getPayload());
        }
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
