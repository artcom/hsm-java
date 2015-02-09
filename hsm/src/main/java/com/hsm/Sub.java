package com.hsm;

import java.util.Collection;
import java.util.HashMap;
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
        mSubMachine.setOrigin(this);
    }

    public Sub(String id, State... states) {
        super(id);
        mSubMachine = new StateMachine(states);
        mSubMachine.setOrigin(this);
    }

    @Override
    void enter(State prev, State next, Map<String, Object> payload) {
        super.enter(prev, next, payload);
        mSubMachine.enterState(prev, next, payload);
    }

    @Override
    void exit(State prev, State next, Map<String, Object> payload) {
        mSubMachine.teardown();
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

    boolean handleWithCasting(Event event) {
        return mSubMachine.handleWithCasting(event);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getId());
        sb.append("/(");
        sb.append(mSubMachine.toString());
        sb.append(")");
        return sb.toString();
    }

    @Override
    void addParent(StateMachine stateMachine) {
        mSubMachine.addParent(stateMachine);
    }

    public String getPath() {
        return mSubMachine.getPathString();
    }

    public StateMachine lca(State targetState) {
        return mSubMachine.findLowestCommonAncestor(targetState);
    }

    @Override
    Collection<? extends State> getDecendantStates() {
        return mSubMachine.getDecendantStates();
    }

}