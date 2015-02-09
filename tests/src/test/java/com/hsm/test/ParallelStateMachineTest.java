package com.hsm.test;


import com.hsm.Action;
import com.hsm.Parallel;
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
import org.mockito.InOrder;

import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ParallelStateMachineTest {

    final static Logger logger = Logger.getLogger(ParallelStateMachineTest.class);

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
    public void canCreateParallelState() {
        // given:
        State capsOn    = new State("caps_on");
        State capsOff   = new State("caps_off");
        StateMachine capsStateMachine = new StateMachine(capsOff, capsOn);

        State numOn     = new State("num_on");
        State numOff    = new State("num_off");
        StateMachine numStateMachine = new StateMachine(numOff, numOn);

        Parallel keyboardOn = new Parallel("keyboard_on", capsStateMachine, numStateMachine);
        StateMachine sm = new StateMachine(keyboardOn);

        // when:
        sm.init();
        logger.debug(sm.toString());

        // then:
        // no exception
    }


    @Test
    public void canSwitchStatesInParallel() {
        // given:
        Action onEnterCapsOn        = mock(Action.class);
        Action onEnterCapsOff       = mock(Action.class);
        Action onEnterNumOn         = mock(Action.class);
        Action onEnterNumOff        = mock(Action.class);
        Action onEnterKeyboardOn    = mock(Action.class);
        Action onEnterKeyboardOff   = mock(Action.class);

        State capsOn    = new State("caps_on")
                .onEnter(onEnterCapsOn)
                .addHandler("capslock", "caps_off", TransitionType.External);
        State capsOff   = new State("caps_off")
                .onEnter(onEnterCapsOff)
                .addHandler("capslock", "caps_on", TransitionType.External);
        StateMachine capsStateMachine = new StateMachine(capsOff, capsOn);

        State numOn     = new State("num_on")
                .onEnter(onEnterNumOn)
                .addHandler("numlock", "num_off", TransitionType.External);
        State numOff    = new State("num_off")
                .onEnter(onEnterNumOff)
                .addHandler("numlock", "num_on", TransitionType.External);
        StateMachine numStateMachine = new StateMachine(numOff, numOn);

        Parallel keyboardOn = new Parallel("keyboard_on", capsStateMachine, numStateMachine)
                .onEnter(onEnterKeyboardOn)
                .addHandler("unplug", "keyboard_off", TransitionType.External);
        State keyboardOff   = new State("keyboard_off")
                .onEnter(onEnterKeyboardOff)
                .addHandler("plug", "keyboard_on", TransitionType.External);
        StateMachine sm = new StateMachine(keyboardOff, keyboardOn);

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
        InOrder inOrder = inOrder(onEnterCapsOff,    onEnterCapsOn, onEnterKeyboardOff,
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
        State p11 = new State("p11");
        State p21 = new State("p21");
        State s1 = new State("s1");
        StateMachine p1 = new StateMachine(p11);
        StateMachine p2 = new StateMachine(p21);
        Parallel s2 = new Parallel("s2", p1, p2);
        Sub s = new Sub("s", s1, s2).addHandler("T1", "p21", TransitionType.External);

        StateMachine sm = new StateMachine(s);
        sm.init();

        sm.handleEvent("T1");
    }
}
