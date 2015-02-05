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
        logger.debug("setup");
        mStateList = new ArrayList<State>(Arrays.asList(states));
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0); 
        }
    }

    public void init() {
        logger.debug("init");
        enterState(null, mInitialState, new HashMap<String, Object>());
    }

    public void teardown() {
        logger.debug("teardown");
        exitState(mCurrentState, null, new HashMap<String, Object>());
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
                if (mCurrentState != null) { 
                    applyEvent(mEventQueue.poll());
                }
            }
            mEventQueueInProgress = false;
        }
    }

    boolean applyEvent(Event event) {
        if (mCurrentState instanceof Sub) { 
            Sub currentSubState = (Sub)mCurrentState;
            if( currentSubState.applyEvent(event)) {
                return true;
            }
        }
        if (mCurrentState.hasHandler(event.getName())){
            logger.debug("applyEvent: "+ event.getName());
            Handler transition = mCurrentState.getHandler(event.getName());
            Action transitionAction = transition.getAction();
            String targetStateId = transition.getTargetStateId();
            if (transitionAction != null) {
                transitionAction.setPreviousState(mCurrentState);
                transitionAction.setNextState(getStateById(targetStateId));
                transitionAction.setPayload(event.getPayload());
                logger.debug("execute action");
                transitionAction.run();
            }

            if ((targetStateId != null) && (transition.getType() == TransitionType.External)) {
                switchState(mCurrentState, getStateById(targetStateId), event.getPayload());
            }
            return true;
        }
        return false;
    }

    private State getStateById(String stateId) {
        for (State state: mStateList) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        return null;
    }

}
