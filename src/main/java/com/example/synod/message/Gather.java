package com.example.synod.message;

public class Gather implements Message {
    private int ballot;
    private int estimateBallot;
    private Boolean estimate;
    private int senderId;

    public Gather (int ballot, int estimateBallot, Boolean estimate, int senderId) {
        this.ballot = ballot;
        this.estimateBallot = estimateBallot;
        this.estimate = estimate;
        this.senderId = senderId;
    }

    public int getBallot() {
        return ballot;
    }

    public int getEstimateBallot() {
        return estimateBallot;
    }

    public Boolean getEstimate() {
        return estimate;
    }

    public int getSenderId() {
        return senderId;
    }
}
