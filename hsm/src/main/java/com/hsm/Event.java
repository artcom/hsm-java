package com.hsm;

import java.util.Map;
import java.util.HashMap;

class Event {

    private final Map<String, Object> mPayload = new HashMap<String, Object>();
    private final String mName;

    public Event(String name) {
        mName = name;
    }

    public Map<String, Object> getPayload() {
        return mPayload;
    }

}
