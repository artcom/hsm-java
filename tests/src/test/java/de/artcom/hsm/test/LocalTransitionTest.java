package de.artcom.hsm.test;


import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import de.artcom.hsm.Action;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionType;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LocalTransitionTest {

    final static Logger LOGGER = Logger.getLogger(LocalTransitionTest.class);

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
    public void canExecuteLocalTransition() {
        // given:
        Action sEnter = mock(Action.class);
        Action s1Enter = mock(Action.class);
        State s1 = new State("s1").onEnter(s1Enter);
        Sub s = new Sub("s", s1).onEnter(sEnter).addHandler("T1", "s1", TransitionType.Local);
        StateMachine sm = new StateMachine(s);
        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        verify(sEnter, times(1)).run();
        verify(s1Enter, times(2)).run();
    }

    @Test
    public void canExecuteLocalTransition2() {
        // given:
        Action sEnter = mock(Action.class);
        Action s1Enter = mock(Action.class);
        State s1 = new State("s1").onEnter(s1Enter).addHandler("T1", "s2", TransitionType.External);
        State s2 = new State("s2").addHandler("T2", "s", TransitionType.Local);

        Sub s = new Sub("s", s1, s2).onEnter(sEnter);
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
    public void cannotExecuteLocalTransition() {
        // given:
        State s1 = new State("s1");
        Sub s = new Sub("s", s1).addHandler("T1", "b1", TransitionType.Local);

        State b1 = new State("b1");
        Sub a = new Sub("a", s, b1);
        StateMachine sm = new StateMachine(a);
        sm.init();

        // when:
        try {
            sm.handleEvent("T1");
            Assert.fail();
        } catch (IllegalStateException e) {
            // then:
        }

        // then:
    }

    @Test
    public void cannotExecuteLocalTransitionToSelf() {
        // given:
        State s1 = new State("s1");
        Sub s = new Sub("s", s1).addHandler("T1", "s", TransitionType.Local);
        StateMachine sm = new StateMachine(s);
        sm.init();

        // when:
        try {
            sm.handleEvent("T1");
            Assert.fail();
        } catch (IllegalStateException e) {
            // then:
        }

    }

}