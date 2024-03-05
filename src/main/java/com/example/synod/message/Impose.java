package com.example.synod.message;

public class Impose {
    private int ballot;
    private Boolean proposal;

    public Impose (int ballot, Boolean proposal) {
        this.ballot = ballot;
        this.proposal = proposal;
    }

    public int getBallot() {
        return ballot;
    }

    public Boolean getProposal() {
        return proposal;
    }
}
