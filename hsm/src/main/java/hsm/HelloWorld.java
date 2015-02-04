package com.hsm;

import org.apache.log4j.Logger;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Level;

public class HelloWorld {

    private Logger logger;

    public HelloWorld() {
        setupLogger();
    }

    private void setupLogger() {
        logger = Logger.getLogger(HelloWorld.class);
        ConsoleAppender console = new ConsoleAppender();
        String pattern = "%d [%p] %C{1}.%M: %m%n";
        console.setLayout(new PatternLayout(pattern)); 
        console.setThreshold(Level.ALL);
        console.activateOptions();
        logger.addAppender(console);
    }

    public String hello() {
        logger.debug("Starting demo");
        return "Hello World";
    }

}
