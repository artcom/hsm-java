package de.artcom.hsm;

import com.google.common.collect.LinkedListMultimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class State<T extends State<T>> {

    final static Logger LOGGER = LoggerFactory.getLogger(State.class);

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

    public T addHandler(String eventName, String targetId, TransitionKind kind, Guard guard) {
        mHandlers.put(eventName, new Handler(targetId, kind, guard));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionKind kind, Action action) {
        mHandlers.put(eventName, new Handler(targetId, kind, action));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionKind kind, Action action, Guard guard) {
        mHandlers.put(eventName, new Handler(targetId, kind, action, guard));
        return getThis();
    }

    public T addHandler(String eventName, String targetId, TransitionKind kind) {
        mHandlers.put(eventName, new Handler(targetId, kind));
        return getThis();
    }

    void setOwner(StateMachine ownerMachine) {
        mOwner = ownerMachine;
    }

    StateMachine getOwner() {
        return mOwner;
    }

    String getId() {
        return mId;
    }

    void enter(State prev, State next, Map<String, Object> payload) {
        LOGGER.debug(getId() + " - enter");
        if (mOnEnterAction != null) {
            mOnEnterAction.setPreviousState(prev);
            mOnEnterAction.setNextState(next);
            mOnEnterAction.setPayload(payload);
            mOnEnterAction.run();
        }
    }

    void exit(State prev, State next, Map<String, Object> payload) {
        LOGGER.debug(getId() + " - exit");
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
            LOGGER.debug(mId + " - handle Event: " + event.getName());
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

    public void emitEvent(String event) {
        emitEvent(event, new HashMap<String, Object>());
    }

    public void emitEvent(String eventName, Map<String, Object> payload) {
        mOwner.getPath().get(0).handleEvent(eventName, payload);
    }
}
