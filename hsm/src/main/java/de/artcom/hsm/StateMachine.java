package de.artcom.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateMachine implements EventHandler {

    static Logger LOGGER = LoggerFactory.getLogger(StateMachine.class);

    private final List<State> mStateList = new ArrayList<State>();
    private final List<State> mDescendantStateList = new ArrayList<State>();
    private String mName;
    private State mInitialState;
    private State mCurrentState;
    private final Queue<Event> mEventQueue = new ConcurrentLinkedQueue<Event>();
    private boolean mEventQueueInProgress = false;
    private final List<StateMachine> mPath = new ArrayList<StateMachine>();
    private State mContainer;
    private ILogger logger;
    public StateMachine(String name, State initialState, State... states) {
        this(initialState, states);
        mName = name;
    }

    public StateMachine(State initialState, State... states) {
        mStateList.addAll(Arrays.asList(states));
        mStateList.add(initialState);
        mInitialState = initialState;
        setOwner();
        generatePath();
        generateDescendantStateList();
        mName = "";
    }

    void setLogger(ILogger log)
    {
        LOGGER = (Logger)log;
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
        init(new HashMap<String, Object>());
    }

    public void init(Map<String, Object> payload) {
        LOGGER.debug(mName + " init");
        if (mInitialState == null) {
            throw new IllegalStateException(mName + " Can't init without states defined.");
        } else {
            mEventQueueInProgress = true;
            if(payload  == null) {
                payload = new HashMap<String, Object>();
            }
            enterState(null, mInitialState, payload);
            mEventQueueInProgress = false;
            processEventQueue();
        }
    }

    void teardown(Map<String, Object> payload) {
        LOGGER.debug(mName + " teardown");
        if(payload == null) {
            payload = new HashMap<String, Object>();
        }
        exitState(mCurrentState, null, payload);
        mCurrentState = null;
    }

    public void teardown() {
        teardown(new HashMap<String, Object>());
    }

    @Override
    public void handleEvent(String event) {
        handleEvent(event, new HashMap<String, Object>());
    }

    @Override
    public void handleEvent(String eventName, Map<String, Object> payload) {
        if(mCurrentState == null) {
            return; // TODO: throw an exception here
        }
        // TODO: make a deep copy of the payload (also do this in Parallel)
        mEventQueue.add(new Event(eventName, payload));
        processEventQueue();
    }

    private void processEventQueue() {
        if (mEventQueueInProgress) {
            return;
        }
        mEventQueueInProgress = true;
        while (mEventQueue.peek() != null) {
            Event event = mEventQueue.poll();
            if (!mCurrentState.handleWithOverride(event)) {
                LOGGER.debug(mName + " nobody handled event: " + event.getName());
            }
        }
        mEventQueueInProgress = false;
    }

    boolean handleWithOverride(Event event) {
        if (mCurrentState != null ) {
            return mCurrentState.handleWithOverride(event);
        } else {
            return false;
        }
    }

    void executeHandler(Handler handler, Event event) {
        LOGGER.debug(mName + " execute handler for event: " + event.getName());

        Action action = handler.getAction();
        State targetState = handler.getTargetState();
        if (targetState == null) {
            throw new IllegalStateException(mName + " cant find target state for transition " + event.getName());
        }
        switch (handler.getKind()) {
            case External:
                doExternalTransition(mCurrentState, targetState, action, event);
                break;
            case Local:
                doLocalTransition(mCurrentState, targetState, action, event);
                break;
            case Internal:
                executeAction(action, mCurrentState, targetState, event.getPayload());
                break;
        }
    }

    private void executeAction(Action action, State previousState, State targetState, Map<String, Object> payload) {
        if (action != null) {
            action.setPreviousState(previousState);
            action.setNextState(targetState);
            action.setPayload(payload);
            action.run();
        }
    }

    private void doExternalTransition(State previousState, State targetState, Action action, Event event) {
        StateMachine lca = findLowestCommonAncestor(targetState);
        lca.switchState(previousState, targetState, action, event.getPayload());
    }

    private void doLocalTransition(State previousState, State targetState, Action action, Event event) {
        if(previousState.getDescendantStates().contains(targetState)) {
            StateMachine stateMachine = findNextStateMachineOnPathTo(targetState);
            stateMachine.switchState(previousState, targetState, action, event.getPayload());
        } else if(targetState.getDescendantStates().contains(previousState)) {
            int targetLevel = targetState.getOwner().getPath().size();
            StateMachine stateMachine = mPath.get(targetLevel);
            stateMachine.switchState(previousState, targetState, action, event.getPayload());
        } else if(previousState.equals(targetState)) {
            //TODO: clarify desired behavior for local transition on self
            //      currently behaves like an internal transition
        } else {
            doExternalTransition(previousState, targetState, action, event);
        }
    }

    private void switchState(State previousState, State nextState, Action action, Map<String, Object> payload) {
        exitState(previousState, nextState, payload);
        executeAction(action, previousState, nextState, payload);
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
        return targetOwner.getPath().get(localLevel);
    }

    private void exitState(State previousState, State nextState, Map<String, Object> payload) {
        mCurrentState.exit(previousState, nextState, payload);
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

    private StateMachine findLowestCommonAncestor(State targetState) {
        if (targetState.getOwner() == null) {
            throw new IllegalStateException(mName + " Target state '" + targetState.getId() + "' is not contained in state machine model.");
        }
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

    public List<State> getAllActiveStates() {
        ArrayList<State> stateList = new ArrayList<State>();
        stateList.add(mCurrentState);
        stateList.addAll(mCurrentState.getAllActiveStates());
        return stateList;
    }


    String getName() {
        return mName;
    }

    void setName(String name) {
        this.mName = name;
    }
}
