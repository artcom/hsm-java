package de.artcom.hsm;


import com.google.common.collect.LinkedListMultimap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class State<T extends State<T>> {

    ILogger LOGGER = new ILogger() {
        @Override
        public void debug(String message) {
            Logger.getAnonymousLogger().log(Level.INFO,message);
        }
    };

    private final String mId;
    private Action mOnEnterAction;
    private Action mOnExitAction;
    private final LinkedListMultimap<String, Handler> mHandlers;
    protected StateMachine mOwner;

    protected T getThis() {
        return (T) this;
    }

    public State(String id) {
        mHandlers = LinkedListMultimap.create();
        mId = id;
    }
    public void setLogger(ILogger log)
    {
        LOGGER = log;
    }
    public T onEnter(Action onEnterAction) {
        mOnEnterAction = onEnterAction;
        return getThis();
    }

    public T onExit(Action onExitAction) {
        mOnExitAction = onExitAction;
        return getThis();
    }

    public T addHandler(String eventName, State target, TransitionKind kind, Guard guard) {
        mHandlers.put(eventName, new Handler(target, kind, guard));
        return getThis();
    }

    public T addHandler(String eventName, State target, TransitionKind kind, Action action) {
        mHandlers.put(eventName, new Handler(target, kind, action));
        return getThis();
    }

    public T addHandler(String eventName, State target, TransitionKind kind, Action action, Guard guard) {
        mHandlers.put(eventName, new Handler(target, kind, action, guard));
        return getThis();
    }

    public T addHandler(String eventName, State target, TransitionKind kind) {
        mHandlers.put(eventName, new Handler(target, kind));
        return getThis();
    }

    void setOwner(StateMachine ownerMachine) {
        mOwner = ownerMachine;
    }

    StateMachine getOwner() {
        return mOwner;
    }

    public String getId() {
        return mId;
    }

    void enter(State prev, State next, Map<String, Object> payload) {
        LOGGER.debug("[" + mOwner.getName() + "] " + getId() + " - enter");
        if (mOnEnterAction != null) {
            mOnEnterAction.setPreviousState(prev);
            mOnEnterAction.setNextState(next);
            mOnEnterAction.setPayload(payload);
            mOnEnterAction.run();
        }
    }

    void exit(State prev, State next, Map<String, Object> payload) {
        LOGGER.debug("[" + mOwner.getName() + "] " + getId() + " - exit");
        if (mOnExitAction != null) {
            mOnExitAction.setPreviousState(prev);
            mOnExitAction.setNextState(next);
            mOnExitAction.setPayload(payload);
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
            LOGGER.debug("[" + mOwner.getName() + "] " + mId + " - handle Event: " + event.getName());
            mOwner.executeHandler(handler, event);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return mId;
    }

    void addParent(StateMachine stateMachine) {
        // do nothing
    }

    Collection<? extends State> getDescendantStates() {
        return new ArrayList<State>();
    }

    public EventHandler getEventHandler() {
        return mOwner.getPath().get(0);
    }

    public List<State> getAllActiveStates() {
        return new ArrayList<State>();
    }
}
