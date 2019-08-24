package com.loop.hyh.core.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HTTPSession {
    private String id;
    private Map<String, Object> attributes;
    
    public HTTPSession(String id){
        this.id = id;
        attributes = new ConcurrentHashMap<>();
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    public String getId(){
        return id;
    }
}
