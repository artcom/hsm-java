package com.hsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StateMachine {

    private final List<State> mStateList;

    public StateMachine(State... states) {
        mStateList = new ArrayList<State>(Arrays.asList(states));
    }


    public void bootUp() {
        mStateList.get(0).enter(null, mStateList.get(0));
    }
}
