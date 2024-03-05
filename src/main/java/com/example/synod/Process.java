package com.example.synod;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;

import com.example.synod.message.Abort;
import com.example.synod.message.Ack;
import com.example.synod.message.Decide;
import com.example.synod.message.Gather;
import com.example.synod.message.Impose;
import com.example.synod.message.Launch;
import com.example.synod.message.Membership;
import com.example.synod.message.Read;

import java.util.ArrayList;
import java.util.Random;

public class Process extends UntypedAbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);// Logger attached to actor

    private int n;// number of processes
    private int i;// id of current process
    private Membership processes;// other processes' references
    private Boolean proposal;
    private int ballot;
    private int readballot;
    private int imposeballot;
    private Boolean estiamte;
    private ArrayList<Pair<Boolean, Integer>> states;

    /**
     * Static method to create an actor
     */
    public static Props createActor(int n, int i) {
        return Props.create(Process.class, () -> new Process(n, i));
    }

    public Process(int n, int i) {
        this.n = n;
        this.i = i;
        this.ballot = i - n;
        this.proposal = null;
        this.readballot = 0;
        this.imposeballot = i - n;
        this.estiamte = null;
        this.initState();

    }

    private void initState() {
        this.states = new ArrayList<Pair<Boolean, Integer>>();
        for (int index = 0; index < this.n; index++) {
            this.states.add(new Pair<Boolean, Integer>(null, 0));
        }
    }

    // private void broadcast (Message message) {
    //     // create super class for all message type (add extend Message for all messages types)
    // }

    private void propose(Boolean v) {
        log.info(this + " - propose(" + v + ")");
        proposal = v;
        ballot += n;
        initState();
    }

    private void receiveRead(Read message) {

    }

    private void receiveAbort(Abort message) {
    }

    private void receiveGather(Gather message) {
    }

    private void receiveImpose(Impose message) {
    }

    private void receiveDecide(Decide message) {
    }

    private void receiveAck(Ack message) {
    }

    public void onReceive(Object message) throws Throwable {
        if (message instanceof Membership) {
            log.info(this + " - membership received");
            Membership m = (Membership) message;
            processes = m;
        } else if (message instanceof Launch) {
            log.info(this + " - launch received");
            propose(true);
        }
    }

    @Override
    public String toString() {
        return "Process #" + i;
    }

}
