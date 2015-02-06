package com.hsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

public class StateMachine {

    final static Logger logger = Logger.getLogger(StateMachine.class);

    private final List<State> mStateList;
    private State mInitialState = null;
    private State mCurrentState;
    private final Queue<Event> mEventQueue = new ConcurrentLinkedQueue<Event>();
    private boolean mEventQueueInProgress = false;

    public StateMachine(State... states) {
        mStateList = Arrays.asList(states);
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0); 
        }
        setOwner();
    }

    public void init() {
        logger.debug("init");
        enterState(null, mInitialState, new HashMap<String, Object>());
    }

    public void teardown() {
        logger.debug("teardown");
        exitState(mCurrentState, null, new HashMap<String, Object>());
    }

    public void handleEvent(String event) {
        handleEvent(event, new HashMap<String, Object>());
    }

    public void handleEvent(String eventName, Map<String, Object> payload) {
        mEventQueue.add(new Event(eventName, payload));
        logger.debug("handleEvent: "+ eventName);
        if (mEventQueueInProgress) {
            //events are already processed
        } else {
            mEventQueueInProgress = true;
            while( (mCurrentState != null) && (mEventQueue.peek() != null) ) {
                //handleWithCasting(mEventQueue.poll());
                mCurrentState.handleWithOverride(mEventQueue.poll());
            }
            mEventQueueInProgress = false;
        }
    }

    boolean handleWithCasting(Event event) {
        if (mCurrentState instanceof Sub) {
            Sub currentSubState = (Sub)mCurrentState;
            if( currentSubState.handleWithCasting(event)) {
                return true;
            }
        }
        Handler handler = mCurrentState.findHandler(event);
        if (handler != null) {
            executeHandler(handler, event);
            return true;
        }
        return false;
    }

    boolean handleWithOverride(Event event) {
        return mCurrentState.handleWithOverride(event);
    }

    void executeHandler(Handler handler, Event event) {
        logger.debug("execute handler for event: " + event.getName());
        Action handlerAction = handler.getAction();
        State targetState = getStateById(handler.getTargetStateId());
        if (handlerAction != null) {
            handlerAction.setPreviousState(mCurrentState);
            handlerAction.setNextState(targetState);
            handlerAction.setPayload(event.getPayload());
            handlerAction.run();
        }
        switch(handler.getType()) {
            case External:
                switchState(mCurrentState, targetState, event.getPayload());
                break;
            case Internal:
                break;
        }
    }

    private void switchState(State previousState, State nextState, Map<String, Object> payload) {
        exitState(previousState, nextState, payload);
        enterState(previousState, nextState, payload);
    }

    private void enterState(State previousState, State nextState, Map<String, Object> payload) {
        if (nextState != null) {
            mCurrentState = nextState;
            nextState.enter(previousState, nextState);
        }
    }

    private void exitState(State previousState, State nextState, Map<String, Object> payload) {
        if (previousState != null) {
            previousState.exit(previousState, nextState);
        }
    }

    private State getStateById(String stateId) {
        for (State state: mStateList) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        return null;
    }

    private void setOwner() {
        for (State state: mStateList) {
            state.setOwner(this);
        }
    }

    @Override
    public String toString() {
        return mCurrentState.toString();
    }
}
