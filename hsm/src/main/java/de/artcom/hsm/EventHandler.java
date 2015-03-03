package de.artcom.hsm;

import java.util.Map;

public interface EventHandler {

    public void handleEvent(String event);
    public void handleEvent(String event, Map<String, Object> payload);

}
