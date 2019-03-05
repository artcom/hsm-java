package de.artcom.hsm.test.helper;

import de.artcom.hsm.ILogger;

import java.util.logging.Level;

public class Logger implements ILogger {


    @Override
    public void debug(String message) {
        java.util.logging.Logger.getAnonymousLogger().log(Level.ALL,message);
    }
}
