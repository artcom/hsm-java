package com.hsm;

public class Sub extends State<Sub> {

    @Override
    protected Sub getThis() {
        return this;
    }

    public Sub(String id) {
        super(id);
    }

    public Sub add() {
        return getThis();
    }
}
