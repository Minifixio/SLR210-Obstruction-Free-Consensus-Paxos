package com.example.synod.message;

public class Impose implements Message {
    private int ballot;
    private Boolean proposal;
    private int senderId;

    public Impose (int ballot, Boolean proposal, int senderId) {
        this.ballot = ballot;
        this.proposal = proposal;
        this.senderId = senderId;
    }

    public int getBallot() {
        return ballot;
    }

    public Boolean getProposal() {
        return proposal;
    }

    public int getSenderId() {
        return senderId;
    }
}
