package com.hsm;

import java.util.Map;

public interface Guard {
    public boolean evaluate(Map<String, Object> payload);
}
