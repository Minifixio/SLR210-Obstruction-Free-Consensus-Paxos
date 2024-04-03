package com.example.synod.message;

public class ContinueAbort implements Message {
    private int ballot;

    public ContinueAbort (int ballot) {
        this.ballot = ballot;
    }

    public int getBallot () {
        return this.ballot;
    }
}
