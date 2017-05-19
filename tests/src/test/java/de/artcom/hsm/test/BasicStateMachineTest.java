package de.artcom.hsm.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.artcom.hsm.Action;
import de.artcom.hsm.Parallel;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionKind;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class BasicStateMachineTest {

    @Test
    public void cannotCreateEmptyStateMachine() {

        try {
            StateMachine sm = new StateMachine(null);
            Assert.fail("NullPointerException should raise when creating a empty StateMachine");
        } catch (NullPointerException npe) {
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
        Action offEnterAction = mock(Action.class);
        State off = new State("off")
                .onEnter(offEnterAction);
        State on = new State("on")
                .addHandler("toggle", off, TransitionKind.External)
                .onExit(onExitAction);
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
        State off = new State("off");
        State on = new State("on")
                .addHandler("toggle", off, TransitionKind.External)
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

        Action offEnterAction = mock(Action.class);
        State off = new State("off")
                .onEnter(offEnterAction);

        Action onExitAction = mock(Action.class);
        State on = new State("on")
                .addHandler("toggle", off, TransitionKind.External)
                .onExit(onExitAction);


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
        State off = new State("off");
        State on = new State("on")
                .addHandler("toggle", off, TransitionKind.External, toggleAction);

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
        State off = new State("off");
        State on = new State("on")
                .addHandler("toggle", off, TransitionKind.External, toggleAction);

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
        State on = new State("on");
        on.addHandler("toggle", on, TransitionKind.Internal, toggleAction)
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
        State off = new State("off");
        State on = new State("on").addHandler("toggle", off, TransitionKind.External);
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
        State b201 = new State("b201");
        Sub b21 = new Sub("b21", b201);
        Sub b1 = new Sub("b1", b21);
        Sub b = new Sub("b", b1);
        Sub bar = new Sub("bar", b);

        State a1 = new State("a1").addHandler("T1", b201, TransitionKind.External);
        Sub a = new Sub("a", a1);
        Sub foo = new Sub("foo", a);

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
        Sub b = new Sub("b", a);
        b.addHandler("T1", b, TransitionKind.Internal, bAction);
        Sub c = new Sub("c", b);
        StateMachine stateMachine = new StateMachine(c);

        // when:
        stateMachine.init();

        // then:
        verify(bAction).run();
    }

    @Test
    public void getAllActiveStates() {
        // given:
        State a11 = new State("a11");
        State a22 = new State("a22");
        State a33 = new State("a33");
        Parallel a1 = new Parallel("a1", new StateMachine(a11), new StateMachine(a22, a33));
        Sub a = new Sub("a", a1);
        StateMachine sm = new StateMachine(a);
        sm.init();

        // when:
        List<State> allActiveStates = sm.getAllActiveStates();

        // then:
        assertThat(allActiveStates, hasItems(a, a1, a11, a22));
        assertThat(allActiveStates, not(hasItems(a33)));
    }

}
