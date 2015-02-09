package de.artcom.hsm.test;

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

import static org.mockito.Mockito.mock;

public class LocalTransitionTest {

    final static Logger logger = Logger.getLogger(BasicStateMachineTest.class);

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
//        Action action = mock(Action.class);
//        State onState = new State("on").addHandler("toggle", "on", TransitionType.Local);

    }

    @Test
    public void simpleSubMachineTest() {
        State s2 = new State("s2");
        State s3 = new State("s3");
        Sub s = new Sub("s", s2, s3).addHandler("T1", "s3", TransitionType.External);

        StateMachine sm = new StateMachine(s);
        sm.init();

        sm.handleEvent("T1");
    }
}
