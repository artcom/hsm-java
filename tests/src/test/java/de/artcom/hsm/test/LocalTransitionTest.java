package de.artcom.hsm.test;

import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;

import de.artcom.hsm.Action;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionKind;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import org.mockito.InOrder;

public class LocalTransitionTest {

    @Test
    public void canExecuteLocalTransition() {
        // given:
        Action sEnter = mock(Action.class);
        Action s1Enter = mock(Action.class);
        State s1 = new State("s1").onEnter(s1Enter);
        Sub s = new Sub("s", s1).onEnter(sEnter).addHandler("T1", s1, TransitionKind.Local);
        StateMachine sm = new StateMachine(s);
        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        verify(sEnter, times(1)).run();
        verify(s1Enter, times(2)).run();
    }

    @Test
    public void canExecuteLocalTransitionToAncestorState() {
        // given:
        Action sEnter = mock(Action.class);
        Action s1Enter = mock(Action.class);
        State s1 = new State("s1").onEnter(s1Enter);
        State s2 = new State("s2");
        Sub s = new Sub("s", s1, s2).onEnter(sEnter);

        s1.addHandler("T1", s2, TransitionKind.External);
        s2.addHandler("T2", s, TransitionKind.Local);

        StateMachine sm = new StateMachine(s);
        sm.init();

        // when:
        sm.handleEvent("T1");
        sm.handleEvent("T2");

        // then:
        verify(sEnter, times(1)).run();
        verify(s1Enter, times(2)).run();
    }

    @Test
    public void canExecuteLocalTransitionWhichResultsInExternal() {
        // given:
        Action sExit = mock(Action.class);
        Action s1Exit = mock(Action.class);
        Action b1Enter = mock(Action.class);

        State s1 = new State("s1").onExit(s1Exit);
        Sub s = new Sub("s", s1).onExit(sExit);

        State b1 = new State("b1").onEnter(b1Enter);
        Sub a = new Sub("a", s, b1);
        StateMachine sm = new StateMachine(a);

        s.addHandler("T1", b1, TransitionKind.Local);

        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        InOrder inOrder = inOrder(s1Exit, sExit, b1Enter);
        inOrder.verify(s1Exit).run();
        inOrder.verify(sExit).run();
        inOrder.verify(b1Enter).run();
    }

}
