package com.hsm.test;

import java.util.HashMap;

import com.hsm.Action;
import com.hsm.State;
import com.hsm.StateMachine;
import com.hsm.Sub;

import org.junit.*;
import static org.mockito.Mockito.*;

import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;


public class BasicStateMachineTest {

    final static Logger logger = Logger.getLogger(BasicStateMachineTest.class);

    @BeforeClass
    public static void setupLogger() {
        ConsoleAppender console = new ConsoleAppender();
        String pattern = "%d [%p] %C{1}.%M: %m%n";
        console.setLayout(new PatternLayout(pattern)); 
        console.setThreshold(Level.ALL);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
    }

    @Test
    public void canCreateEmptyStateMachine() {
        StateMachine sm = new StateMachine();
        sm.init();
        sm.teardown();
    }

    @Test
    public void chainTest() {
        Sub state = new Sub("foo")
            .foo()
            .add()
            .bar()
            .onEnter(mock(Action.class))
            .onExit(mock(Action.class));
    }

    @Test
    public void initialStateIsEntered() {
        Action enterAction = mock(Action.class);
        State on = new State("on")
            .onEnter(enterAction);
        StateMachine sm = new StateMachine(on);
        sm.init();
        verify(enterAction).run();
    }

    @Test
    public void canSwitchStates() {
        Action onExitAction = mock(Action.class);
        //Action toggleAction = mock(Action.class);
        Action toggleAction = new Action();
        State on = new State("on")
            .addHandler("toggle", "off", toggleAction)
            .onExit(onExitAction);

        Action offEnterAction = mock(Action.class);
        State off = new State("off")
            .onEnter(offEnterAction);

        StateMachine sm = new StateMachine(on, off);
        sm.init();
        sm.handleEvent("toggle", new HashMap<String, Object>());

        //verify(toggleAction).run();
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
