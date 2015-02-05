package com.hsm.test;

import java.util.Map;
import java.util.HashMap;

import com.hsm.Action;
import com.hsm.TransitionType;
import com.hsm.State;
import com.hsm.StateMachine;
import com.hsm.Sub;

import org.junit.*;
import org.junit.Assert;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

import static org.mockito.Mockito.*;
import org.mockito.InOrder;
//import org.mockito.internal.InOrderImpl;

import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;

public class SubStateMachineTest {

    final static Logger logger = Logger.getLogger(SubStateMachineTest.class);

    @BeforeClass
    public static void setupLogger() {
        ConsoleAppender console = new ConsoleAppender();
        String pattern = "%d [%p] %C{1}.%M: %m%n";
        console.setLayout(new PatternLayout(pattern)); 
        console.setThreshold(Level.ALL);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);
    }

    @Test
    public void canCreateSubStateMachine() {
        //given:
        State loud = new State("loud");
        State quiet = new State("quiet");
        Sub on = new Sub("on", new StateMachine(quiet, loud));
        StateMachine sm = new StateMachine(on);
        //then:
        sm.init();
    }

    @Test
    public void canTransitionSubStates() {
        //given:
        Action onEnterLoud = mock(Action.class);
        Action onEnterQuiet = mock(Action.class);
        Action onEnterOn = mock(Action.class);
        Action onEnterOff = mock(Action.class);
        State loud = new State("loud")
            .addHandler("volume_down", "quiet", TransitionType.External)
            .onEnter(onEnterLoud);
        State quiet = new State("quiet")
            .addHandler("volume_up", "loud", TransitionType.External)
            .onEnter(onEnterQuiet);
        Sub on = new Sub("on", new StateMachine(quiet, loud))
            .addHandler("switched_off", "off", TransitionType.External)
            .onEnter(onEnterOn);
        State off = new State("off")
            .addHandler("switched_on", "on", TransitionType.External)
            .onEnter(onEnterOff);
        StateMachine sm = new StateMachine(off, on);
        sm.init();
        //when:
        sm.handleEvent("switched_on");
        sm.handleEvent("volume_up");
        sm.handleEvent("switched_off");
        sm.handleEvent("switched_on");
        //then:

        InOrder inOrder = inOrder(onEnterOff, onEnterOn, onEnterQuiet, onEnterLoud);
        inOrder.verify(onEnterOff).run();
        inOrder.verify(onEnterOn).run();
        inOrder.verify(onEnterQuiet).run();
        inOrder.verify(onEnterLoud).run();
        inOrder.verify(onEnterOff).run();
        inOrder.verify(onEnterOn).run();
        inOrder.verify(onEnterQuiet).run();
    }

}
