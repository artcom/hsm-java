package com.hsm;

import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class State<T extends State<T>> {

    final static Logger logger = Logger.getLogger(StateMachine.class);

    private final String mId;
    private Action mOnEnterAction;
    private Action mOnExitAction;
    private final Map<String, Handler> mHandlers = new HashMap<String, Handler>();

    protected T getThis() {
        return (T) this;
    }

    public State(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
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

    public T addHandler(String eventName, String targetId, TransitionType type, Action action) {
        mHandlers.put(eventName, new Handler(targetId, type, action));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionType type) {
        mHandlers.put(eventName, new Handler(targetId, type));
        return getThis();
    }

    boolean hasHandler(String eventName) {
        return mHandlers.containsKey(eventName);
    }

    Handler getHandler(String eventName) {
        return mHandlers.get(eventName);
    }

}
