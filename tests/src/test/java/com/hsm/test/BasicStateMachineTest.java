package com.hsm.test;

import java.util.Map;
import java.util.HashMap;

import com.hsm.Action;
import com.hsm.TransitionType;
import com.hsm.State;
import com.hsm.StateMachine;
import com.hsm.Sub;

import org.junit.*;
import org.junit.Assert;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

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
        //given:
        Action enterAction = mock(Action.class);
        State on = new State("on")
            .onEnter(enterAction);
        StateMachine sm = new StateMachine(on);
        //when:
        sm.init();
        //then:
        verify(enterAction).run();
    }

    @Test
    public void eventsWithoutPayloadCauseStateTransition() {
        //given:
        Action onExitAction = mock(Action.class);
        State on = new State("on")
            .addHandler("toggle", "off", TransitionType.External)
            .onExit(onExitAction);
        Action offEnterAction = mock(Action.class);
        State off = new State("off")
            .onEnter(offEnterAction);
        StateMachine sm = new StateMachine(on, off);
        sm.init();

        //when:
        sm.handleEvent("toggle");

        //then:
        verify(onExitAction).run();
        verify(offEnterAction).run();
    }

    @Test
    public void eventsWithPayloadCauseStateTransition() {
        //given:
        Action onExitAction = mock(Action.class);
        State on = new State("on")
            .addHandler("toggle", "off", TransitionType.External)
            .onExit(onExitAction);
        Action offEnterAction = mock(Action.class);
        State off = new State("off")
            .onEnter(offEnterAction);
        StateMachine sm = new StateMachine(on, off);
        sm.init();

        //when:
        sm.handleEvent("toggle", new HashMap<String, Object>());

        //then:
        verify(onExitAction).run();
        verify(offEnterAction).run();
    }

    @Test
    public void actionsAreCalledOnTransitionsWithPayload() {
        //given:
        final boolean[] actionGotCalled = { false };
        Action toggleAction = new Action() {
            @Override
            public void run() {
                actionGotCalled[0] = true;
                assertThat(getPayload(), notNullValue());
                Assert.assertTrue(getPayload().containsKey("foo"));
                Assert.assertTrue(getPayload().get("foo") instanceof String);
                Assert.assertTrue(((String)getPayload().get("foo")).equals("bar"));
            }
        };
        State on = new State("on")
            .addHandler("toggle", "off", TransitionType.External, toggleAction);
        State off = new State("off");
        StateMachine sm = new StateMachine(on, off);
        sm.init();
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("foo", "bar");

        //when:
        sm.handleEvent("toggle", payload);

        //then:
        if (!actionGotCalled[0]) {
            Assert.fail("action was not called");
        }
    }

    @Test
    public void actionsAreCalledAlwaysWithValidPayload() {
        //given:
        final boolean[] actionGotCalled = { false };
        Action toggleAction = new Action() {
            @Override
            public void run() {
                actionGotCalled[0] = true;
                assertThat(getPayload(), notNullValue());
                Assert.assertTrue(getPayload().isEmpty());
            }
        };
        State on = new State("on")
            .addHandler("toggle", "off", TransitionType.External, toggleAction);
        State off = new State("off");
        StateMachine sm = new StateMachine(on, off);
        sm.init();

        //when:
        sm.handleEvent("toggle");

        //then:
        if (!actionGotCalled[0]) {
            Assert.fail("action was not called");
        }
    }

    @Test
    public void actionsCanBeInternal() {
        //given:
        Action onExitAction = mock(Action.class);
        Action toggleAction = mock(Action.class);
        State on = new State("on")
            .addHandler("toggle", "on", TransitionType.Internal, toggleAction)
            .onExit(onExitAction);
        StateMachine sm = new StateMachine(on);
        sm.init();

        //when:
        sm.handleEvent("toggle");

        //then:
        verify(toggleAction).run();
        verifyZeroInteractions(onExitAction);
    }

    @Test
    public void eventsAreHandledAccordingToRunToCompletion() {
        //given:
        State rawEgg = new State("rawEgg");
        State softEgg = new State("softEgg");
        State hardEgg = new State("hardEgg");
        final StateMachine sm = new StateMachine(rawEgg, softEgg, hardEgg);

        Action boilAction = new Action() {
            @Override
            public void run() {
                sm.handleEvent("boil_too_long");
            }
        };

        Action onEnterHardEgg = mock(Action.class);
        Action onEnterSoftEgg = mock(Action.class);

        softEgg.onEnter(onEnterSoftEgg);
        hardEgg.onEnter(onEnterHardEgg);
        rawEgg.addHandler("boil", "softEgg", TransitionType.External, boilAction);

        Action boilTooLongAction = mock(Action.class);
        rawEgg.addHandler("boil_too_long", "hardEgg", TransitionType.External, boilTooLongAction);

        Action boilTooLongAction2 = mock(Action.class);
        softEgg.addHandler("boil_too_long", "softEgg", TransitionType.Internal, boilTooLongAction2);

        sm.init();

        //when:
        sm.handleEvent("boil");

        //then:
        verifyZeroInteractions(onEnterHardEgg);
        verifyZeroInteractions(boilTooLongAction);
        verify(onEnterSoftEgg).run();
        verify(boilTooLongAction2).run();
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
