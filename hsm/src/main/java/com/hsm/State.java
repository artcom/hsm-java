package com.hsm;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class State<T extends State<T>> {

    final static Logger logger = Logger.getLogger(StateMachine.class);

    private final String mId;

    private Action mOnEnterAction;
    private Action mOnExitAction;

    private final Map<String, Transition> mTransitions = new HashMap<String, Transition>();

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
        logger.debug("enter: " + getId());
        if (mOnEnterAction != null) {
            mOnEnterAction.setPreviousState(prev);
            mOnEnterAction.setNextState(next);
            mOnEnterAction.run();
        }
    }

    void exit(State prev, State next) {
        logger.debug("exit: " + getId());
        if (mOnExitAction != null) {
            mOnExitAction.setPreviousState(prev);
            mOnExitAction.setNextState(next);
            mOnExitAction.run();
        }
    }

    public T onEnter(Action onEnterAction) {
        mOnEnterAction = onEnterAction;
        return getThis();
    }

    public T onExit(Action onExitAction) {
        mOnExitAction = onExitAction;
        return getThis();
    }

    public T addHandler(String eventName, String targetId, Action action) {
        mTransitions.put(eventName, new Transition(targetId, action));
        return getThis();
    }

    public T addHandler(String eventName, String targetId) {
        mTransitions.put(eventName, new Transition(targetId));
        return getThis();
    }

    boolean hasHandler(String eventName) {
        return mTransitions.containsKey(eventName);
    }

    Transition getHandler(String eventName) {
        return mTransitions.get(eventName);
    }

}
