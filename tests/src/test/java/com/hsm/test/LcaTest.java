package com.hsm.test;

import com.hsm.Action;
import com.hsm.State;
import com.hsm.StateMachine;
import com.hsm.Sub;
import com.hsm.TransitionType;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.mock;

public class LcaTest {

    final static Logger logger = Logger.getLogger(LcaTest.class);

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

    @Ignore
    @Test
    public void pathTest() {
        State a1 = new State("a1");
        Sub a = new Sub("a", a1);
        Sub foo = new Sub("foo", a);
        Sub bar = new Sub("bar", foo);
        StateMachine sm = new StateMachine(bar);
        sm.init();

        logger.debug("path of a: " + a.getPath());
        logger.debug("path of foo: " + foo.getPath());
        logger.debug("path of bar: " + bar.getPath());
        logger.debug("path of sm: " + sm.getPathString());
    }

    @Ignore
    @Test
    public void lcaTest() {
        State a1 = new State("a1");
        Sub a = new Sub("a", a1);
        State c1 = new State("c1");
        Sub c = new Sub("c", c1);
        Sub foo = new Sub("foo", a, c);

        State b1 = new State("b1");
        Sub b = new Sub("b", b1);
        Sub bar = new Sub("bar", b);
        StateMachine sm = new StateMachine(foo, bar);
        sm.init();

        StateMachine lca = a.lca(c1);
        logger.debug("result: " + lca);
    }

    @Ignore
    @Test
    public void testDecendantStates() {
        State a1 = new State("a1");
        Sub a = new Sub("a", a1);
        State c1 = new State("c1");
        Sub c = new Sub("c", c1);
        Sub foo = new Sub("foo", a, c);

        State b1 = new State("b1");
        Sub b = new Sub("b", b1);
        Sub bar = new Sub("bar", b);
        StateMachine sm = new StateMachine(foo, bar);
        sm.init();
    }

    @Test
    public void testLowestCommonAncestor1() {
        // given:
        Action exitA1 = mock(Action.class);
        Action exitA = mock(Action.class);
        Action exitFoo = mock(Action.class);
        State a1 = new State("a1").onExit(exitA1).addHandler("T1", "b1", TransitionType.External);
        Sub a = new Sub("a", a1).onExit(exitA);
        Sub foo = new Sub("foo", a).onExit(exitFoo);

        Action enterB1 = mock(Action.class);
        Action enterB = mock(Action.class);
        Action enterBar = mock(Action.class);
        State b1 = new State("b1").onEnter(enterB1);
        Sub b = new Sub("b", b1).onEnter(enterB);
        Sub bar = new Sub("bar", b).onEnter(enterBar);
        StateMachine sm = new StateMachine(foo, bar);
        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:

    }

}
