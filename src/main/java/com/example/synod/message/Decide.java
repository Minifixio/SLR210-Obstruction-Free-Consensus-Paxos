package com.example.synod.message;

public class Decide implements Message {
    private Boolean proposal;

    public Decide(Boolean proposal) {
        this.proposal = proposal;
    }

    public Boolean getProposal() {
        return this.proposal;
    }
}
