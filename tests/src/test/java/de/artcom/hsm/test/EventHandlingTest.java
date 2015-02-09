package de.artcom.hsm.test;

import de.artcom.hsm.Action;
import de.artcom.hsm.Guard;
import de.artcom.hsm.State;
import de.artcom.hsm.StateMachine;
import de.artcom.hsm.Sub;
import de.artcom.hsm.TransitionType;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class EventHandlingTest {

    final static Logger logger = Logger.getLogger(EventHandlingTest.class);

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
    public void runToCompletionTest() {
        // given:
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

        // when:
        sm.handleEvent("boil");

        // then:
        verifyZeroInteractions(onEnterHardEgg);
        verifyZeroInteractions(boilTooLongAction);
        verify(onEnterSoftEgg).run();
        verify(boilTooLongAction2).run();
    }

    @Test
    public void dontBubbleUpTest() {
        // given:
        Action enterA2 = mock(Action.class);
        Action enterB = mock(Action.class);

        State a1 = new State("a1").addHandler("T1", "a2", TransitionType.External, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                return payload.containsKey("foo");
            }
        });
        State a2 = new State("a2").onEnter(enterA2);
        Sub a = new Sub("a", a1, a2).addHandler("T1", "b", TransitionType.External);

        State b = new State("b").onEnter(enterB);
        StateMachine sm = new StateMachine(a, b);

        sm.init();

        // when:
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("foo", "bar");
        sm.handleEvent("T1", payload);

        // then:
        verify(enterA2).run();
        verifyZeroInteractions(enterB);
    }

    @Test
    public void bubbleUpTest() {
        // given:
        Action enterA2 = mock(Action.class);
        Action enterB = mock(Action.class);

        State a1 = new State("a1").addHandler("T1", "a2", TransitionType.External, new Guard() {
            @Override
            public boolean evaluate(Map<String, Object> payload) {
                return payload.containsKey("foo");
            }
        });
        State a2 = new State("a2").onEnter(enterA2);
        Sub a = new Sub("a", a1, a2).addHandler("T1", "b", TransitionType.External);

        State b = new State("b").onEnter(enterB);
        StateMachine sm = new StateMachine(a, b);

        sm.init();

        // when:
        sm.handleEvent("T1");

        // then:
        verify(enterB).run();
        verifyZeroInteractions(enterA2);
    }
}






















