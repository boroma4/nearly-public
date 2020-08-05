package com.ukrainianboyz.nearly.db.enums;

import java.util.HashMap;
import java.util.Map;

public enum Status {
    REQUEST_SENT(0),
    ACCEPTED(1),
    BLOCKED_BY_SECOND(2),
    BLOCKED_BY_FIRST(3),
    BLOCKED_BY_BOTH(4);

    private final int value;
    private static Map map = new HashMap<>();


    Status(int value) {
        this.value = value;
    }

    static {
        for (Status status : Status.values()) {
            map.put(status.value, status);
        }
    }


    public static Status valueOf(int status){
        return (Status) map.get(status);
    }

    public int getValue(){
        return value;
    }

}
