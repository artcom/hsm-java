package com.hsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class StateMachine {

    private final List<State> mStateList;
    private State mInitialState = null;
    private State mCurrentState;

    public StateMachine(State... states) {
        mStateList = new ArrayList<State>(Arrays.asList(states));
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0); 
        }
    }

    public void init() {
        enterState(null, mInitialState, null);
    }

    public void teardown() {
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

    public void handleEvent(String eventName, Map<String, Object> payload) {
        if ((mCurrentState != null) && 
            (mCurrentState.hasHandler(eventName))){
        
            Transition transition = mCurrentState.getHandler(eventName);
            
            Action transitionAction = transition.getAction();
            String targetStateId = transition.getTargetStateId();
            if (transitionAction != null) {
                transitionAction.setPreviousState(mCurrentState);
                transitionAction.setNextState(getStateById(targetStateId));
                transitionAction.setPayload(payload);
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
