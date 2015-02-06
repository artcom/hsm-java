package com.hsm;

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

    void enter(State prev, State next) {
        super.enter(prev, next);
        mSubMachine.init();
    }

    void exit(State prev, State next) {
        mSubMachine.teardown();
        super.exit(prev, next);
    }

    @Override
    boolean handleWithOverride(Event event) {
        if(mSubMachine.handleWithOverride(event)) {
            return true;
        } else {
            return super.handleWithOverride(event);
        }
    }

    boolean handleWithCasting(Event event) {
        return mSubMachine.handleWithCasting(event);
    }

}
