package de.artcom.hsm;

class Handler {

    private final String mTargetStateId;
    private final TransitionKind mKind;
    private Guard mGuard;
    private Action mAction;

    public Handler(String targetStateId, TransitionKind kind, Action action, Guard guard) {
        mTargetStateId = targetStateId;
        mKind = kind;
        mAction = action;
        mGuard = guard;
    }

    public Handler(String targetStateId, TransitionKind kind, Guard guard) {
        mTargetStateId = targetStateId;
        mKind = kind;
        mGuard = guard;
    }

    public Handler(String targetStateId, TransitionKind kind, Action action) {
        mTargetStateId = targetStateId;
        mKind = kind;
        mAction = action;
    }

    public Handler(String targetStateId, TransitionKind kind) {
        mTargetStateId = targetStateId;
        mKind = kind;
    }

    public boolean evaluate(Event event) {
        if (mGuard != null) {
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

    public TransitionKind getKind() {
        return mKind;
    }

}
