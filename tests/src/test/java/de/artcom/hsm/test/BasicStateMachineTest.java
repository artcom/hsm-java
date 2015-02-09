package de.artcom.hsm.test;

import de.artcom.hsm.Action;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.TransitionType;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.spockframework.util.Assert.fail;

public class BasicStateMachineTest {

    final static Logger logger = Logger.getLogger(BasicStateMachineTest.class);

    @BeforeClass
    public static void setupLogger() {
        ConsoleAppender console = new ConsoleAppender();
        String pattern = "%d [%p] %C{1}.%M: %m%n";
        console.setLayout(new PatternLayout(pattern)); 
        console.setThreshold(Level.ALL);
        console.activateOptions();
        Logger.getRootLogger().removeAllAppenders();
        Logger.getRootLogger().addAppender(console);
    }

    @Test
    public void cannotInitEmptyStateMachine() {
        // given:
        StateMachine sm = new StateMachine();

        // when:
        try {
            sm.init();
            Assert.fail("IllegalStateException should raise when initing a empty StateMachine");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void canChainMethods() {
        State state = new State("foo")
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
    public void currentStateIsExited() {
        //given:
        Action exitAction = mock(Action.class);
        State on = new State("on")
            .onExit(exitAction);
        StateMachine sm = new StateMachine(on);
        sm.init();
        //when:
        sm.teardown();
        //then:
        verify(exitAction).run();
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
    public void impossibleTransitionTest() {
        // given:
        Action onExitAction = mock(Action.class);
        State on = new State("on")
            .addHandler("toggle", "off", TransitionType.External)
            .onExit(onExitAction);
        Action offEnterAction = mock(Action.class);
        StateMachine sm = new StateMachine(on);
        sm.init();

        // when:
        try {
            sm.handleEvent("toggle");
            Assert.fail("expected NullpointerException but nothing happnend");
        } catch (IllegalStateException npe) {
        }
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
                assertThat(mPayload, notNullValue());
                Assert.assertTrue(mPayload.containsKey("foo"));
                Assert.assertTrue(mPayload.get("foo") instanceof String);
                Assert.assertTrue(((String)mPayload.get("foo")).equals("bar"));
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
                assertThat(mPayload, notNullValue());
                Assert.assertTrue(mPayload.isEmpty());
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
    public void noMatchingStateAvailable() {
        // given:
        State on = new State("on").addHandler("toggle", "off", TransitionType.External);
        StateMachine sm = new StateMachine(on);
        sm.init();

        // when:
        try {
            sm.handleEvent("toggle");
            Assert.fail("expected IllegalStateException since target State was not part of StateMachine");
        } catch(IllegalStateException e) {
        }
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
