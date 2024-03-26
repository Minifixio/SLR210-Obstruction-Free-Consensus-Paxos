package com.example.synod.message;

import akka.actor.ActorRef;
import java.util.List;

public class Membership implements Message {

    public List<ActorRef> references;

    public Membership(List<ActorRef> references) {
        this.references = references;
    }

}
