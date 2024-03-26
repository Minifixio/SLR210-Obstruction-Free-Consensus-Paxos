package com.example.synod.message;

public class Read implements Message {
    private int ballot;
    public Read (int ballot) {
        this.ballot = ballot;
    }

    public int getBallot () {
        return this.ballot;
    }
}
