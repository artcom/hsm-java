package de.artcom.hsm;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateMachine {

    final static Logger logger = Logger.getLogger(StateMachine.class);

    private final List<State> mStateList;

    private final List<State> mDecendantStateList = new ArrayList<State>();

    private State mInitialState = null;

    private State mCurrentState;

    private final Queue<Event> mEventQueue = new ConcurrentLinkedQueue<Event>();

    private boolean mEventQueueInProgress = false;

    private final List<StateMachine> mPath = new ArrayList<StateMachine>();

    private State mOrigin;


    public StateMachine(State... states) {
        mStateList = Arrays.asList(states);
        if (!mStateList.isEmpty()) {
            mInitialState = mStateList.get(0);
        }
        setOwner();
        generatePath();
        generateDecendantStateList();
    }

    void setOrigin(State origin) {
        mOrigin = origin;
    }

    State getOrigin() {
        return mOrigin;
    }

    private void generateDecendantStateList() {
        mDecendantStateList.addAll(mStateList);
        for (State state : mStateList) {
            mDecendantStateList.addAll(state.getDecendantStates());
        }
    }

    private void generatePath() {
        mPath.add(0, this);
        for (State state : mStateList) {
            state.addParent(this);
        }
    }

    public void init() {
        logger.debug("init");
        if (mInitialState != null) {
            enterState(null, mInitialState, new HashMap<String, Object>());
        } else {
            throw new IllegalStateException("Can't init without states defined.");
        }
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
        logger.debug("handleEvent: " + eventName);
        if (mEventQueueInProgress) {
            //events are already processed
        } else {
            mEventQueueInProgress = true;
            while (mEventQueue.peek() != null) {
                mCurrentState.handleWithOverride(mEventQueue.poll());
            }
            mEventQueueInProgress = false;
        }
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

        switch (handler.getType()) {
            case External:
                StateMachine lca = findLowestCommonAncestor(targetState);
                lca.switchState(mCurrentState, targetState, event.getPayload());
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
        if(mCurrentState.getDecendantStates().contains(targetState)) {
            StateMachine stateMachine = findNextStateMachineOnPathTo(targetState);
            stateMachine.switchState(mCurrentState, targetState, event.getPayload());
        } else if(targetState.getDecendantStates().contains(mCurrentState)) {
            int targetLevel = targetState.getOwner().getPath().size();
            StateMachine stateMachine = mPath.get(targetLevel);
            stateMachine.switchState(mCurrentState, targetState, event.getPayload());
        } else {
            throw new IllegalStateException("Target state is no sub state of " + mCurrentState.getId() + " therefore a local transition is not Possible.");
        }
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
        return findNextStateMachineOnPathTo(targetState).getOrigin();
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
        for (State state : mDecendantStateList) {
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
            sb.append(" ");
            sb.append(stateMachine.toString());
            sb.append("\r\n");
        }
        return sb.toString();
    }

    public void addParent(StateMachine stateMachine) {
        logger.debug("addParent " + stateMachine.toString());
        mPath.add(0, stateMachine);
        for (State state : mStateList) {
            state.addParent(stateMachine);
        }
    }

    // TODO: make it package private
    public StateMachine findLowestCommonAncestor(State targetState) {
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

    List<State> getDecendantStates() {
        return mDecendantStateList;
    }

}
