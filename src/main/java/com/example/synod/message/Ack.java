package com.example.synod.message;

public class Ack {
    private int ballot;

    public Ack (int ballot) {
        this.ballot = ballot;
    }

    public int getBallot () {
        return this.ballot;
    }
}
