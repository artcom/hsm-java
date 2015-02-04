package com.hsm;

public class State<T extends State<T>> {

    private final String mId;

    private Action mOnEnterAction;
    private Action mOnExitAction;

    protected T getThis() {
        return (T) this;
    }

    public State(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }

    public T foo() {
        return getThis();
    }

    public T bar() {
        return getThis();
    }

    void enter(State prev, State next) {
        mOnEnterAction.previousState(prev);
        mOnEnterAction.nextState(next);
        mOnEnterAction.run();
    }

    void exit(State prev, State next) {
        mOnExitAction.previousState(prev).nextState(next).run();
    }

    public T onEnter(Action onEnterAction) {
        mOnEnterAction = onEnterAction;
        return getThis();
    }

    public T onExit(Action onExitAction) {
        mOnExitAction = onExitAction;
        return getThis();
    }

}
