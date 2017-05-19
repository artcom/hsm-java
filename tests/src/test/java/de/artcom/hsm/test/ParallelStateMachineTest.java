package de.artcom.hsm.test;

import org.junit.Test;
import org.mockito.InOrder;

import de.artcom.hsm.Action;
import de.artcom.hsm.Parallel;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionKind;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

public class ParallelStateMachineTest {

    @Test
    public void canCreateParallelState() {
        // given:
        State capsOn = new State("caps_on");
        State capsOff = new State("caps_off");
        StateMachine capsStateMachine = new StateMachine(capsOff, capsOn);

        State numOn = new State("num_on");
        State numOff = new State("num_off");
        StateMachine numStateMachine = new StateMachine(numOff, numOn);

        Parallel keyboardOn = new Parallel("keyboard_on", capsStateMachine, numStateMachine);
        StateMachine sm = new StateMachine(keyboardOn);

        // when:
        sm.init();
        System.out.println(sm.toString());

        // then:
        // no exception
    }


    @Test
    public void canSwitchStatesInParallel() {
        // given:
        Action onEnterCapsOn = mock(Action.class);
        Action onEnterCapsOff = mock(Action.class);
        Action onEnterNumOn = mock(Action.class);
        Action onEnterNumOff = mock(Action.class);
        Action onEnterKeyboardOn = mock(Action.class);
        Action onEnterKeyboardOff = mock(Action.class);

        State capsOn = new State("caps_on")
                .onEnter(onEnterCapsOn);

        State capsOff = new State("caps_off")
                .onEnter(onEnterCapsOff);

        StateMachine capsStateMachine = new StateMachine(capsOff, capsOn);

        capsOn.addHandler("capslock", capsOff, TransitionKind.External);
        capsOff.addHandler("capslock", capsOn, TransitionKind.External);

        State numOn = new State("num_on")
                .onEnter(onEnterNumOn);

        State numOff = new State("num_off")
                .onEnter(onEnterNumOff);

        StateMachine numStateMachine = new StateMachine(numOff, numOn);

        numOn.addHandler("numlock", numOff, TransitionKind.External);
        numOff.addHandler("numlock", numOn, TransitionKind.External);

        Parallel keyboardOn = new Parallel("keyboard_on", capsStateMachine, numStateMachine)
                .onEnter(onEnterKeyboardOn);
        State keyboardOff = new State("keyboard_off")
                .onEnter(onEnterKeyboardOff);
        StateMachine sm = new StateMachine(keyboardOff, keyboardOn);

        keyboardOn.addHandler("unplug", keyboardOff, TransitionKind.External);
        keyboardOff.addHandler("plug", keyboardOn, TransitionKind.External);

        sm.init();

        // when:
        sm.handleEvent("plug");
        sm.handleEvent("capslock");
        sm.handleEvent("capslock");
        sm.handleEvent("numlock");
        sm.handleEvent("unplug");
        sm.handleEvent("capslock");
        sm.handleEvent("plug");

        // then:
        InOrder inOrder = inOrder(onEnterCapsOff, onEnterCapsOn, onEnterKeyboardOff,
                onEnterKeyboardOn, onEnterNumOff, onEnterNumOn);

        inOrder.verify(onEnterKeyboardOff).run();
        inOrder.verify(onEnterKeyboardOn).run();
        inOrder.verify(onEnterCapsOff).run();
        inOrder.verify(onEnterNumOff).run();
        inOrder.verify(onEnterCapsOn).run();
        inOrder.verify(onEnterCapsOff).run();
        inOrder.verify(onEnterNumOn).run();
        inOrder.verify(onEnterKeyboardOff).run();
        inOrder.verify(onEnterKeyboardOn).run();
        inOrder.verify(onEnterCapsOff).run();
        inOrder.verify(onEnterNumOff).run();
    }

    @Test
    public void anotherParallelStateTest() {
        // given:
        Action p11Enter = mock(Action.class);
        Action p21Enter = mock(Action.class);
        State p11 = new State("p11").onEnter(p11Enter);
        State p21 = new State("p21").onEnter(p21Enter);
        State s1 = new State("s1");
        StateMachine p1 = new StateMachine(p11);
        StateMachine p2 = new StateMachine(p21);
        Parallel s2 = new Parallel("s2", p1, p2);
        Sub s = new Sub("s", s1, s2).addHandler("T1", p21, TransitionKind.External);

        StateMachine sm = new StateMachine(s);
        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        verify(p11Enter).run();
        verify(p21Enter).run();
    }

    @Test
    public void parallelStatesCanEmitEventInEnter() {
        // given:
        final State p1 = new State("p1");
        final State p2 = new State("p2");
        Action p1Enter = new Action() {
            @Override
            public void run(){
                p1.getEventHandler().handleEvent("foo");
            }
        };
        Action p2Action = mock(Action.class);
        p1.onEnter(p1Enter);
        p2.addHandler("foo", p2, TransitionKind.Internal, p2Action);
        Parallel p = new Parallel("p", 
            new StateMachine(p1),
            new StateMachine(p2)
        );
        StateMachine sm = new StateMachine(p);

        // when:
        sm.init();

        // then:
        verify(p2Action).run();
    }

    @Test
    public void parallelStatesCanEmitEventInExitAndHandleAction() {
        // given:
        final State p1 = new State("p1");
        final State p2 = new State("p2");
        Action p1Action = new Action() {
            @Override
            public void run(){
                p1.getEventHandler().handleEvent("foo");
            }
        };
        Action p2Action = mock(Action.class);
        p1.onExit(p1Action);
        p2.addHandler("foo", p2, TransitionKind.Internal, p2Action);
        Parallel p = new Parallel("p", 
            new StateMachine(p1),
            new StateMachine(p2)
        );
        StateMachine sm = new StateMachine(p);

        // when:
        sm.init();
        sm.teardown();

        // then:
        verify(p2Action).run();
    }

    @Test
    public void parallelStatesCanEmitEventInExitButIsNotHandledInFinishedStates() {
        // given:
        final State p1 = new State("p1");
        final State p2 = new State("p2");
        Action p1Action = new Action() {
            @Override
            public void run(){
                p1.getEventHandler().handleEvent("foo");
            }
        };
        Action p2Action = mock(Action.class);
        p1.onExit(p1Action);
        p2.addHandler("foo", p2, TransitionKind.Internal, p2Action);
        Parallel p = new Parallel("p", 
            new StateMachine(p2),
            new StateMachine(p1)
        );
        StateMachine sm = new StateMachine(p);

        // when:
        sm.init();
        sm.teardown();

        // then:
        verify(p2Action, never()).run();
    }


}
