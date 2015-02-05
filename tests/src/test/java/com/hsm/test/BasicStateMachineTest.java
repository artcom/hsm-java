package com.hsm.test;

import java.util.HashMap;

import com.hsm.Action;
import com.hsm.State;
import com.hsm.StateMachine;
import com.hsm.Sub;

import org.junit.Test;
import static org.mockito.Mockito.*;

public class BasicStateMachineTest {

    @Test
    public void chainTest() {
        Sub state = new Sub("foo");
        state
            .foo()
            .add()
            .bar()
            .onEnter(new Action() {
                @Override
                public void run() {
                    System.out.println("onEnter");
                }
            })
            .onExit(new Action() {
                @Override
                public void run() {
                    System.out.println("exit");
                }
            });
    }

    @Test
    public void startStateMachineTest() {
        State on = new State("on");
        Action enterAction = mock(Action.class);
        on.onEnter(enterAction);

        StateMachine sm = new StateMachine(on);
        sm.init();
        verify(enterAction).run();
    }

    @Test
    public void canSwitchStates() {
        Action onExitAction = mock(Action.class);
        Action toggleAction = mock(Action.class);
        State on = new State("on")
            .addHandler("toggle", "off", toggleAction)
            .onExit(onExitAction);

        Action offEnterAction = mock(Action.class);
        State off = new State("off")
            .onEnter(offEnterAction);

        StateMachine sm = new StateMachine(on, off);
        sm.init();
        sm.handleEvent("toggle", new HashMap<String, Object>());

        verify(toggleAction).run();
        verify(onExitAction).run();
        verify(offEnterAction).run();
    }

//    abstract class MemoState extends State<MemoState> {
//
//        public MemoState(String id) {
//            super(id);
//            Action enterAction = new Action();
//            onEnter(new Action() {
//                @Override
//                public void run() {
//                    super.run();
//                    doEnter(mPreviousState, mNextState);
//                }
//            });
//            onExit(new Action() {
//                @Override
//                public void run() {
//                    super.run();
//                    doEnter(mPreviousState, mNextState);
//                }
//            });
//        }
//
//        void doEnter(State prev, State next) {
//
//        }
//
//        void onExit(State prev, State next) {
//
//        }
//
//    }
}
