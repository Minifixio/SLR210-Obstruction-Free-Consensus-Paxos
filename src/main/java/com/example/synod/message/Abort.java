package com.example.synod.message;

public class Abort {
    private int ballot;

    public Abort (int ballot) {
        this.ballot = ballot;
    }

    public int getBallot () {
        return this.ballot;
    }
}
