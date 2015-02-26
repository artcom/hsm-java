package de.artcom.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateMachine {

    final static Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

    private final List<State> mStateList;
    private final List<State> mDescendantStateList = new ArrayList<State>();
    private State mInitialState;
    private State mCurrentState;
    private final Queue<Event> mEventQueue = new ConcurrentLinkedQueue<Event>();
    private boolean mEventQueueInProgress = false;
    private final List<StateMachine> mPath = new ArrayList<StateMachine>();
    private State mContainer;

    public StateMachine(State... states) {
        mStateList = Arrays.asList(states);
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0);
        }
        setOwner();
        generatePath();
        generateDescendantStateList();
    }

    void setContainer(State container) {
        mContainer = container;
    }

    State getContainer() {
        return mContainer;
    }

    private void generateDescendantStateList() {
        mDescendantStateList.addAll(mStateList);
        for (State state : mStateList) {
            mDescendantStateList.addAll(state.getDescendantStates());
        }
    }

    private void generatePath() {
        mPath.add(0, this);
        for (State state : mStateList) {
            state.addParent(this);
        }
    }

    public void init() {
        LOGGER.debug("init");
        if (mInitialState == null) {
            throw new IllegalStateException("Can't init without states defined.");
        } else {
            enterState(null, mInitialState, new HashMap<String, Object>());
        }
    }

    public void teardown() {
        LOGGER.debug("teardown");
        exitState(mCurrentState, null, new HashMap<String, Object>());
    }

    public void handleEvent(String event) {
        handleEvent(event, new HashMap<String, Object>());
    }

    public void handleEvent(String eventName, Map<String, Object> payload) {
        if(mCurrentState == null) {
            return; // TODO: throw an exception here
        }
        mEventQueue.add(new Event(eventName, payload));
        if (mEventQueueInProgress) {
            //events are already processed
        } else {
            mEventQueueInProgress = true;
            while (mEventQueue.peek() != null) {
                if(!mCurrentState.handleWithOverride(mEventQueue.poll())) {
                    LOGGER.debug("nobody handled event: " + eventName);
                }
            }
            mEventQueueInProgress = false;
        }
    }

    boolean handleWithOverride(Event event) {
        return mCurrentState.handleWithOverride(event);
    }

    void executeHandler(Handler handler, Event event) {
        LOGGER.debug("execute handler for event: " + event.getName());

        Action handlerAction = handler.getAction();
        State targetState = getStateById(handler.getTargetStateId());
        if (handlerAction != null) {
            handlerAction.setPreviousState(mCurrentState);
            handlerAction.setNextState(targetState);
            handlerAction.setPayload(event.getPayload());
            handlerAction.run();
        }

        switch (handler.getKind()) {
            case External:
                doExternalTransition(targetState, event);
                break;
            case Local:
                doLocalTransition(targetState, event);
                break;
            case Internal:
                // no state switch required
                break;
        }
    }

    private void doLocalTransition(State targetState, Event event) {
        if(mCurrentState.getDescendantStates().contains(targetState)) {
            StateMachine stateMachine = findNextStateMachineOnPathTo(targetState);
            stateMachine.switchState(mCurrentState, targetState, event.getPayload());
        } else if(targetState.getDescendantStates().contains(mCurrentState)) {
            int targetLevel = targetState.getOwner().getPath().size();
            StateMachine stateMachine = mPath.get(targetLevel);
            stateMachine.switchState(mCurrentState, targetState, event.getPayload());
        } else if(mCurrentState.equals(targetState)) {
            //TODO: clarify desired behavior for local transition on self
            //      currently behaves like an internal transition
        } else {
            doExternalTransition(targetState, event);
        }
    }

    private void doExternalTransition(State targetState, Event event) {
        StateMachine lca = findLowestCommonAncestor(targetState);
        lca.switchState(mCurrentState, targetState, event.getPayload());
    }

    void switchState(State previousState, State nextState, Map<String, Object> payload) {
        exitState(previousState, nextState, payload);
        enterState(previousState, nextState, payload);
    }

    void enterState(State previousState, State targetState, Map<String, Object> payload) {
        int targetLevel = targetState.getOwner().getPath().size();
        int localLevel = mPath.size();
        State nextState;
        if (targetLevel < localLevel) {
            nextState = mInitialState;
        } else if (targetLevel == localLevel) {
            nextState = targetState;
        } else { // if targetLevel > localLevel
            nextState = findNextStateOnPathTo(targetState);
        }
        if (mStateList.contains(nextState)) {
            mCurrentState = nextState;
        } else {
            mCurrentState = mInitialState;
        }
        mCurrentState.enter(previousState, targetState, payload);
    }

    private State findNextStateOnPathTo(State targetState) {
        return findNextStateMachineOnPathTo(targetState).getContainer();
    }

    private StateMachine findNextStateMachineOnPathTo(State targetState) {
        int localLevel = mPath.size();
        StateMachine targetOwner = targetState.getOwner();
        StateMachine nextStateMachineOnPath = targetOwner.getPath().get(localLevel);
        return nextStateMachineOnPath;
    }

    private void exitState(State previousState, State nextState, Map<String, Object> payload) {
        mCurrentState.exit(previousState, nextState, payload);
    }

    State getStateById(String stateId) {
        StateMachine stateMachine = mPath.get(0);
        if (!stateMachine.equals(this)) {
            return stateMachine.getStateById(stateId);
        }
        for (State state : mDescendantStateList) {
            if (state.getId().equals(stateId)) {
                return state;
            }
        }
        throw new IllegalStateException("cant find State with ID: " + stateId + " in " + toString());
    }

    private void setOwner() {
        for (State state : mStateList) {
            state.setOwner(this);
        }
    }

    @Override
    public String toString() {
        if (mCurrentState == null) {
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
        for (StateMachine stateMachine : mPath) {
            sb.append(Integer.toString(++count));
            sb.append(' ');
            sb.append(stateMachine.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    void addParent(StateMachine stateMachine) {
        mPath.add(0, stateMachine);
        for (State state : mStateList) {
            state.addParent(stateMachine);
        }
    }

    // TODO: make it package private
    StateMachine findLowestCommonAncestor(State targetState) {
        List<StateMachine> targetPath = targetState.getOwner().getPath();
        int size = mPath.size();
        for (int i = 1; i < size; i++) {
            try {
                StateMachine targetAncestor = targetPath.get(i);
                StateMachine localAncestor = mPath.get(i);
                if (!targetAncestor.equals(localAncestor)) {
                    return mPath.get(i - 1);
                }
            } catch (IndexOutOfBoundsException e) {
                return mPath.get(i - 1);
            }
        }
        return this;
    }

    List<State> getDescendantStates() {
        return mDescendantStateList;
    }

}
