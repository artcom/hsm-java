package com.hsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class StateMachine {

    final static Logger logger = Logger.getLogger(StateMachine.class);

    private final List<State> mStateList;
    private State mInitialState = null;
    private State mCurrentState;

    public StateMachine(State... states) {
        logger.debug("setup");
        mStateList = new ArrayList<State>(Arrays.asList(states));
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0); 
        }
    }

    public void init() {
        logger.debug("init");
        enterState(null, mInitialState, null);
    }

    public void teardown() {
        logger.debug("teardown");
    }

    private void switchState(State previousState, State nextState, Map<String, Object> payload) {
        exitState(previousState, nextState, payload);
        enterState(previousState, nextState, payload);
    }

    private void enterState(State previousState, State nextState, Map<String, Object> payload) {
        if (nextState != null) {
            mCurrentState = nextState;
            logger.debug("enterState: "+ mCurrentState.getId());
            nextState.enter(previousState, nextState);
        }
    }

    private void exitState(State previousState, State nextState, Map<String, Object> payload) {
        if (previousState != null) {
            logger.debug("exitState: "+ previousState.getId());
            previousState.exit(previousState, nextState);
        }
    }

    public void handleEvent(String eventName, Map<String, Object> payload) {
        if ((mCurrentState != null) && 
            (mCurrentState.hasHandler(eventName))){
            logger.debug("handleEvent: "+ eventName);
        
            Transition transition = mCurrentState.getHandler(eventName);
            
            Action transitionAction = transition.getAction();
            String targetStateId = transition.getTargetStateId();
            if (transitionAction != null) {
                transitionAction.setPreviousState(mCurrentState);
                transitionAction.setNextState(getStateById(targetStateId));
                transitionAction.setPayload(payload);
                logger.debug("execute action");
                transitionAction.run();
            }

            if (targetStateId != null) {
                switchState(mCurrentState, getStateById(targetStateId), payload);
            }
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

    public void handleEvent(String event) {
    }

}
