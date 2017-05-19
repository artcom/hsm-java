package de.artcom.hsm;

class Handler {

    private final State mTargetState;
    private final TransitionKind mKind;
    private Guard mGuard;
    private Action mAction;

    public Handler(State targetState, TransitionKind kind, Action action, Guard guard) {
        mTargetState = targetState;
        mKind = kind;
        mAction = action;
        mGuard = guard;
    }

    public Handler(State targetState, TransitionKind kind, Guard guard) {
        mTargetState = targetState;
        mKind = kind;
        mGuard = guard;
    }

    public Handler(State targetState, TransitionKind kind, Action action) {
        mTargetState = targetState;
        mKind = kind;
        mAction = action;
    }

    public Handler(State targetState, TransitionKind kind) {
        mTargetState = targetState;
        mKind = kind;
    }

    public boolean evaluate(Event event) {
        if (mGuard != null) {
            return mGuard.evaluate(event.getPayload());
        }
        return true;
    }

    public State getTargetState() {
        return mTargetState;
    }

    public Action getAction() {
        return mAction;
    }

    public TransitionKind getKind() {
        return mKind;
    }

}
