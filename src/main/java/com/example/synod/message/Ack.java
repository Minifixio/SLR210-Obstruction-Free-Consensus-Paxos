package com.example.synod.message;

public class Ack implements Message {
    private int ballot;

    public Ack (int ballot) {
        this.ballot = ballot;
    }

    public int getBallot () {
        return this.ballot;
    }
}
