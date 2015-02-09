package de.artcom.hsm;

import java.util.Map;

class Event {

    private final Map<String, Object> mPayload;

    private final String mName;

    public Event(String name, Map<String, Object> payload) {
        mName = name;
        mPayload = payload;
    }

    public Map<String, Object> getPayload() {
        return mPayload;
    }

    public String getName() {
        return mName;
    }

}
