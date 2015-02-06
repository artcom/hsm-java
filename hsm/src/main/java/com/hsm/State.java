package com.hsm;

import com.google.common.collect.LinkedListMultimap;

import org.apache.log4j.Logger;

public class State<T extends State<T>> {

    final static Logger logger = Logger.getLogger(StateMachine.class);

    private final String mId;
    private Action mOnEnterAction;
    private Action mOnExitAction;
    private final LinkedListMultimap<String, Handler> mHandlers;
    private StateMachine mOwner;

    protected T getThis() {
        return (T) this;
    }

    public State(String id) {
        mHandlers = LinkedListMultimap.create();
        mId = id;
    }

    public T onEnter(Action onEnterAction) {
        mOnEnterAction = onEnterAction;
        return getThis();
    }

    public T onExit(Action onExitAction) {
        mOnExitAction = onExitAction;
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionType type, Guard guard) {
        mHandlers.put(eventName, new Handler(targetId, type, guard));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionType type, Action action) {
        mHandlers.put(eventName, new Handler(targetId, type, action));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionType type, Action action, Guard guard) {
        mHandlers.put(eventName, new Handler(targetId, type, action, guard));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionType type) {
        mHandlers.put(eventName, new Handler(targetId, type));
        return getThis();
    }

    void setOwner(StateMachine ownerMachine) {
        mOwner = ownerMachine;
    }

    String getId() {
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

    Handler findHandler(Event event) {
        for (Handler handler : mHandlers.get(event.getName())) {
            if (handler.evaluate(event)) {
                return handler;
            }
        }
        return null;
    }

    boolean handleWithOverride(Event event) {
        Handler handler = findHandler(event);
        if (handler != null) {
            logger.debug("handle Event: "+ event.getName());
            //TODO: find lca
            mOwner.executeHandler(handler, event);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return mId.toString();
    }

    void addParent(StateMachine stateMachine) {
        // do nothing
    }
}
