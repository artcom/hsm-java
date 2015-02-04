package com.hsm.test;

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
        sm.bootUp();
        verify(enterAction).run();
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
