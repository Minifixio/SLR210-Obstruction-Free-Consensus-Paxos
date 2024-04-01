package com.example.synod.message;

public class Init implements Message{
    
    private long initTime;

    public Init (long initTime) {
        this.initTime = initTime;
    }

    public long getInitTime() {
        return initTime;
    }
}
