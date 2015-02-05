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

    boolean hasHandler(String eventName) {
        logger.debug("check for handler for: " + eventName);
        if (mSubMachine.hasHandler(eventName)) {
            logger.debug("found handler in sub state");
            return true;
        }
        return super.hasHandler(eventName);
    }

    Transition getHandler(String eventName) {
        logger.debug("get handler for: " + eventName);
        if (mSubMachine.hasHandler(eventName)) {
            logger.debug("return handler from sub state");
            return mSubMachine.getHandler(eventName);
        }
        return super.getHandler(eventName);
    }

    boolean applyEvent(Event event) {
        return mSubMachine.applyEvent(event);
    }

}
