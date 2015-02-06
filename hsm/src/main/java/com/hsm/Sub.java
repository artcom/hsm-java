package com.hsm;

import java.util.Collection;

public class Sub extends State<Sub> {

    private final StateMachine mSubMachine;

    @Override
    protected Sub getThis() {
        return this;
    }

    public Sub(String id, StateMachine subMachine) {
        super(id);
        mSubMachine = subMachine;
    }

    public Sub(String id, State... states) {
        super(id);
        mSubMachine = new StateMachine(states);
    }

    void enter(State prev, State next) {
        super.enter(prev, next);
        mSubMachine.init();
//        mSubMachine.enterState(prev, next);
    }

    void exit(State prev, State next) {
        mSubMachine.teardown();
        super.exit(prev, next);
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