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
    private final List<State> mDecendantStateList = new ArrayList<State>();
    private State mInitialState = null;
    private State mCurrentState;
    private final Queue<Event> mEventQueue = new ConcurrentLinkedQueue<Event>();
    private boolean mEventQueueInProgress = false;
    private final List<StateMachine> mPath = new ArrayList<StateMachine>();


    public StateMachine(State... states) {
        mStateList = Arrays.asList(states);
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0); 
        }
        setOwner();
        generatePath();
        generateDecendantStateList();
    }

    private void generateDecendantStateList() {
        mDecendantStateList.addAll(mStateList);
        for(State state : mStateList) {
            mDecendantStateList.addAll(state.getDecendantStates());
        }
    }

    private void generatePath() {
        mPath.add(0, this);
        for(State state : mStateList) {
            state.addParent(this);
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
        if(mCurrentState != null) {
            return mCurrentState.handleWithOverride(event);
        }
        return false;
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
                StateMachine lca = findLowestCommonAncestor(targetState);
                lca.switchState(mCurrentState, targetState, event.getPayload());
                break;
            case Internal:
                break;
        }
    }

    void switchState(State previousState, State nextState, Map<String, Object> payload) {
        exitState(previousState, nextState, payload);
        enterState(previousState, nextState, payload);
    }

    void enterState(State previousState, State targetState, Map<String, Object> payload) {
//        int targetLevel = targetState.getOwner().getPath().size(); // 3
//        int localLevel = mPath.size();                             // 1
//        if(targetLevel < localLevel) {
//            mCurrentState = mInitialState;
//        } else if(targetLevel == localLevel) {
//            mCurrentState = targetState;
//        } else {
////            mCurrentState = targetState.getOwner().getPath().get(localLevel).getOwner();
//        }



        if (targetState != null) {
            mCurrentState = targetState;
            targetState.enter(previousState, targetState);
        }
    }

    private void exitState(State previousState, State nextState, Map<String, Object> payload) {
        if(mCurrentState != null) {
            mCurrentState.exit(previousState, nextState);
        }
    }

    State getStateById(String stateId) {
        StateMachine stateMachine = mPath.get(0);
        if(!stateMachine.equals(this)) {
            return stateMachine.getStateById(stateId);
        }
        for (State state: mDecendantStateList) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        return null; // TODO: throw exception here!
    }

    private void setOwner() {
        for (State state: mStateList) {
            state.setOwner(this);
        }
    }

    @Override
    public String toString() {
        if(mCurrentState == null) {
            return mInitialState.toString();
        }
        return mCurrentState.toString();
    }

    List<StateMachine> getPath() {
        return mPath;
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        sb.append("\r\n");
        for(StateMachine stateMachine : mPath) {
            sb.append(Integer.toString(++count));
            sb.append(" ");
            sb.append(stateMachine.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public void addParent(StateMachine stateMachine) {
        logger.debug("addParent " + stateMachine.toString());
        mPath.add(0, stateMachine);
        for(State state : mStateList) {
            state.addParent(stateMachine);
        }
    }

    // TODO: make it package private
    public StateMachine findLowestCommonAncestor(State targetState) {
        int size = mPath.size();
        for (int i = 1; i < size; i++) {
            StateMachine targetAncestor = targetState.getOwner().getPath().get(i);
            StateMachine localAncestor = mPath.get(i);
            if(!targetAncestor.equals(localAncestor)) {
                return mPath.get(i - 1);
            }
        }
        return this;
    }

    List<State> getDecendantStates() {
        return mDecendantStateList;
    }

}
