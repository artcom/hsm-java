package de.artcom.hsm;

import java.util.Collection;
import java.util.Map;

public class Sub extends State<Sub> {

    private final StateMachine mSubMachine;

    @Override
    protected Sub getThis() {
        return this;
    }

    public Sub(String id, StateMachine subMachine) {
        super(id);
        mSubMachine = subMachine;
        mSubMachine.setContainer(this);
    }

    public Sub(String id, State initialState, State... states) {
        super(id);
        mSubMachine = new StateMachine(initialState, states);
        mSubMachine.setContainer(this);
    }

    @Override
    void enter(State prev, State next, Map<String, Object> payload) {
        super.enter(prev, next, payload);
        mSubMachine.enterState(prev, next, payload);
    }

    @Override
    void exit(State prev, State next, Map<String, Object> payload) {
        mSubMachine.teardown(payload);
        super.exit(prev, next, payload);
    }

    @Override
    boolean handleWithOverride(Event event) {
        if (mSubMachine.handleWithOverride(event)) {
            return true;
        } else {
            return super.handleWithOverride(event);
        }
    }

    @Override
    public String toString() {
        return getId() + "/(" + mSubMachine.toString() + ")";
    }

    @Override
    void addParent(StateMachine stateMachine) {
        mSubMachine.addParent(stateMachine);
    }

    @Override
    Collection<? extends State> getDescendantStates() {
        return mSubMachine.getDescendantStates();
    }

}