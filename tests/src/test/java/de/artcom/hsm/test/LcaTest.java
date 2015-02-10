package de.artcom.hsm.test;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InOrder;

import de.artcom.hsm.Action;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionType;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class LcaTest {

    final static Logger LOGGER = Logger.getLogger(LcaTest.class);

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
    public void testLowestCommonAncestor1() {
        // given:
        Action exitA1 = mock(Action.class);
        Action exitA = mock(Action.class);
        Action exitFoo = mock(Action.class);
        State a1 = new State("a1").onExit(exitA1).addHandler("T1", "bar", TransitionType.External);
        Sub a = new Sub("a", a1).onExit(exitA);
        Sub foo = new Sub("foo", a).onExit(exitFoo);

        Action enterB21 = mock(Action.class);
        Action enterB201 = mock(Action.class);
        Action enterB1 = mock(Action.class);
        Action enterB = mock(Action.class);
        Action enterBar = mock(Action.class);
        State b201 = new State("b201").onEnter(enterB201);
        Sub b21 = new Sub("b21", b201).onEnter(enterB21);
        Sub b1 = new Sub("b1", b21).onEnter(enterB1);
        Sub b = new Sub("b", b1).onEnter(enterB);
        Sub bar = new Sub("bar", b).onEnter(enterBar);
        StateMachine sm = new StateMachine(foo, bar);
        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        InOrder inOrder = inOrder(exitA, exitA1, exitFoo, enterB, enterB1, enterBar, enterB21, enterB201);
        inOrder.verify(exitA1).run();
        inOrder.verify(exitA).run();
        inOrder.verify(exitFoo).run();
        inOrder.verify(enterBar).run();
        inOrder.verify(enterB).run();
        inOrder.verify(enterB1).run();
        inOrder.verify(enterB21).run();
        inOrder.verify(enterB201).run();
    }

    @Test
    public void testLowestCommonAncestor2() {
        // given:
        Action exitA1 = mock(Action.class);
        Action exitA = mock(Action.class);
        Action exitFoo = mock(Action.class);
        State a1 = new State("a1").onExit(exitA1).addHandler("T1", "b201", TransitionType.External);
        Sub a = new Sub("a", a1).onExit(exitA);
        Sub foo = new Sub("foo", a).onExit(exitFoo);

        Action enterB21 = mock(Action.class);
        Action enterB201 = mock(Action.class);
        Action enterB1 = mock(Action.class);
        Action enterB = mock(Action.class);
        Action enterBar = mock(Action.class);
        State b201 = new State("b201").onEnter(enterB201);
        Sub b21 = new Sub("b21", b201).onEnter(enterB21);
        Sub b1 = new Sub("b1", b21).onEnter(enterB1);
        Sub b = new Sub("b", b1).onEnter(enterB);
        Sub bar = new Sub("bar", b).onEnter(enterBar);
        StateMachine sm = new StateMachine(foo, bar);
        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        InOrder inOrder = inOrder(exitA, exitA1, exitFoo, enterB, enterB1, enterBar, enterB21, enterB201);
        inOrder.verify(exitA1).run();
        inOrder.verify(exitA).run();
        inOrder.verify(exitFoo).run();
        inOrder.verify(enterBar).run();
        inOrder.verify(enterB).run();
        inOrder.verify(enterB1).run();
        inOrder.verify(enterB21).run();
        inOrder.verify(enterB201).run();
    }

}
