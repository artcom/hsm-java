package com.hsm.test;

import com.hsm.State;
import com.hsm.StateMachine;
import com.hsm.Sub;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.Test;

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

    @Test
    public void pathTest() {
        logger.debug("create State a1");
        State a1 = new State("a1");
        logger.debug("create Sub a");
        Sub a = new Sub("a", a1);
        logger.debug("create Sub foo");
        Sub foo = new Sub("foo", a);
        logger.debug("create StateMachine");
        StateMachine sm = new StateMachine(foo);
        sm.init();

        logger.debug("path of a: " + a.getPath());
        logger.debug("path of foo: " + foo.getPath());
        logger.debug("path of sm: " + sm.getPath());
    }

}
