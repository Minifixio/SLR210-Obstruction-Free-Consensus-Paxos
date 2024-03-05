package com.example.synod.message;

public class Gather {
    private int ballot;
    private int estimateBallot;
    private Boolean estimate;

    public Gather (int ballot, int estimateBallot, Boolean estimate) {
        this.ballot = ballot;
        this.estimateBallot = estimateBallot;
        this.estimate = estimate;
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
}
