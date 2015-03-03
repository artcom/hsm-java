package de.artcom.hsm.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import de.artcom.hsm.Action;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionKind;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class BasicStateMachineTest {

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
                .addHandler("toggle", "off", TransitionKind.External)
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
                .addHandler("toggle", "off", TransitionKind.External)
                .onExit(onExitAction);
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
                .addHandler("toggle", "off", TransitionKind.External)
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
        final boolean[] actionGotCalled = {false};
        Action toggleAction = new Action() {
            @Override
            public void run() {
                actionGotCalled[0] = true;
                assertThat(mPayload, notNullValue());
                Assert.assertTrue(mPayload.containsKey("foo"));
                Assert.assertTrue(mPayload.get("foo") instanceof String);
                Assert.assertTrue(((String) mPayload.get("foo")).equals("bar"));
            }
        };
        State on = new State("on")
                .addHandler("toggle", "off", TransitionKind.External, toggleAction);
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
        final boolean[] actionGotCalled = {false};
        Action toggleAction = new Action() {
            @Override
            public void run() {
                actionGotCalled[0] = true;
                assertThat(mPayload, notNullValue());
                Assert.assertTrue(mPayload.isEmpty());
            }
        };
        State on = new State("on")
                .addHandler("toggle", "off", TransitionKind.External, toggleAction);
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
                .addHandler("toggle", "on", TransitionKind.Internal, toggleAction)
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
        State on = new State("on").addHandler("toggle", "off", TransitionKind.External);
        StateMachine sm = new StateMachine(on);
        sm.init();

        // when:
        try {
            sm.handleEvent("toggle");
            Assert.fail("expected IllegalStateException since target State was not part of StateMachine");
        } catch (IllegalStateException e) {
        }
    }

    @Test
    public void canGetPathString() {
        // given:
        State a1 = new State("a1").addHandler("T1", "b201", TransitionKind.External);
        Sub a = new Sub("a", a1);
        Sub foo = new Sub("foo", a);

        State b201 = new State("b201");
        Sub b21 = new Sub("b21", b201);
        Sub b1 = new Sub("b1", b21);
        Sub b = new Sub("b", b1);
        Sub bar = new Sub("bar", b);
        StateMachine sm = new StateMachine(foo, bar);

        // when:
        String pathString = sm.getPathString();

        // then:
        assertThat(pathString, notNullValue());
    }

    @Test
    public void enumTest() {
        // just for the code coverage (^__^)
        TransitionKind local = TransitionKind.valueOf("Local");
        assertThat(local, equalTo(TransitionKind.Local));
    }

    @Test
    public void emittedEventTestHandledFromTopStateMachine() {
        class SampleState extends State<SampleState> {

            public SampleState(String id) {
                super(id);
                onEnter(new Action() {
                    @Override
                    public void run() {
                        SampleState.this.getEventHandler().handleEvent("T1");
                    }
                });
            }
        }

        // given:
        Action bAction = mock(Action.class);
        SampleState a1 = new SampleState("a1");

        Sub a = new Sub("a", a1);
        Sub b = new Sub("b", a).addHandler("T1", "b", TransitionKind.Internal, bAction);
        Sub c = new Sub("c", b);
        StateMachine stateMachine = new StateMachine(c);

        // when:
        stateMachine.init();

        // then:
        verify(bAction).run();
    }

}
