package com.example.synod;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;

import com.example.synod.message.Abort;
import com.example.synod.message.Ack;
import com.example.synod.message.Crash;
import com.example.synod.message.Decide;
import com.example.synod.message.Gather;
import com.example.synod.message.Hold;
import com.example.synod.message.Impose;
import com.example.synod.message.Launch;
import com.example.synod.message.Membership;
import com.example.synod.message.Read;

import java.util.ArrayList;
import java.util.Random;

public class Process extends UntypedAbstractActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this); // Logger attached to actor

    private int n;// number of processes
    private int i;// id of current process
    private Membership processes;// other processes' references
    private Boolean proposal;
    private int ballot;
    private int readballot;
    private int imposeballot;
    private Boolean estimate;
    private ArrayList<Pair<Boolean, Integer>> states;

    private Boolean faultProne;
    private Boolean crashed;
    private float crashProbability;

    private Boolean onHold;

    private Boolean decided;
    private int ackReceived;

    private long initTime;
    private Boolean debug;

    /**
     * Static method to create an actor
     */
    public static Props createActor(int n, int i, float crashProbability, Boolean debug) {
        return Props.create(Process.class, () -> new Process(n, i, crashProbability, debug));
    }

    public Process(int n, int i, float crashProbability, Boolean debug) {
        this.n = n;
        this.i = i;
        this.ballot = i - n;
        this.proposal = null;
        this.readballot = 0;
        this.imposeballot = i - n;
        this.estimate = null;
        this.faultProne = false;
        this.crashed = false;
        this.crashProbability = crashProbability;
        this.onHold = false;
        this.decided = false;
        this.ackReceived = 0;
        this.initTime = System.currentTimeMillis();
        this.debug = debug;
        this.initState();
    }

    private void initState() {
        this.states = new ArrayList<Pair<Boolean, Integer>>();
        for (int index = 0; index < this.n; index++) {
            this.states.add(null);
        }
    }

    public Boolean isFaultProne() {
        return faultProne;
    }

    public Boolean isOnHold() {
        return onHold;
    }

    // private void broadcast (Message message) {
    //     // create super class for all message type (add extend Message for all messages types)
    // }

    private void propose(Boolean v) {

        if (onHold) {
            return;
        }
        
        if (debug) log.info(this + " - propose(" + v + ")");

        proposal = v;
        ballot += n;
        initState();
        for (ActorRef actor : processes.references) {
            // avoid sending a message to itself
            if (actor != getSelf()) {
                actor.tell(new Read(ballot), getSelf());
            }
        }
    }

    private void receiveRead(Read message) {
        if (debug)  log.info(this + " - read received");
        int newBallot = message.getBallot();
        if (newBallot < readballot) {
            getSender().tell(new Abort(newBallot), getSelf());
        } else {
            readballot = newBallot;
            getSender().tell(new Gather(newBallot, imposeballot, estimate, i), getSelf());
        }

    }

    // TODO : Handle the abort case
    private void receiveAbort(Abort message) {
        if (debug) log.info(this + " - abort received");
        return;
    }

    private void receiveGather(Gather message) {
        if (debug) log.info(this + " - gather received");
        states.set(message.getSenderId(), new Pair<Boolean, Integer>(message.getEstimate(), message.getEstimateBallot()));
        if (debug) log.info(this + " - states : " + states);
        
        // check if the process has received enough messages
        int count = 0;
        // count the non-null states
        for (Pair<Boolean, Integer> state : states) {
            if (state != null) {
                count++;
            }
        }
        if (count > n / 2) {
            if (debug) log.info(this + " - received enough messages");
            int maxBallot = 0;
            Boolean maxEstimate = null;
            for (Pair<Boolean, Integer> state : states) {
                if (state != null && state.second() > maxBallot) {
                    maxBallot = state.second();
                    maxEstimate = state.first();
                }
            }
            if (maxEstimate != null) {
                proposal = maxEstimate;
            }

            initState();
            for (ActorRef actor : processes.references) {
                actor.tell(new Impose(ballot, proposal, i), getSelf());
            }
        }
    }

    private void receiveImpose(Impose message) {
        if (debug) log.info(this + " - impose received");
        int newBallot = message.getBallot();
        if (readballot > newBallot || imposeballot > newBallot) {
            getSender().tell(new Abort(newBallot), getSelf());
        } else {
            estimate = message.getProposal();
            imposeballot = newBallot;
            getSender().tell(new Ack(newBallot), getSelf());
        }
    }

    // TODO : Handle how to handle a decide message
    private void receiveDecide(Decide message) {

        if (decided) {
            return;
        }

        // send a Decide message to all processes
        decided = true;

        for (ActorRef actor : processes.references) {
            actor.tell(message, getSelf());
        }
        log.info(this + " - decided " + message.getProposal() + " in " + (System.currentTimeMillis() - initTime) + "ms");
    }

    private void receiveAck(Ack message) {
        // if majority of ack received, send a Decide message to all processes 
        ackReceived++;

        if (ackReceived > n / 2) {
            ackReceived = 0;
            if (debug) log.info(this + " - majority of ack received");
            Decide decide = new Decide(proposal);
            for (ActorRef actor : processes.references) {
                actor.tell(decide, getSelf());
            }
        }
    }

    private void receiveLaunch() {
        if (debug) log.info(this + " - launch received");
        // pick a random value and propose it
        Random rand = new Random();
        Boolean v = rand.nextBoolean();
        propose(v);
    }

    private void crash() {
        if (Math.random() < crashProbability) {
            if (debug) log.info(this + " - CRASHED");
            crashed = true;
        }
    }


    // TODO: Maybe make messages extend a super class
    public void onReceive(Object message) throws Throwable {

        // check if the process has already decided
        if (decided) {
            return;
        }

        // check if the process is fault prone and trigger a possible crash
        if (faultProne) {
            crash();
        }
        // if the process is crashed, do not process any message
        if (crashed) {
            return;
        }

        // handle the different types of messages
        if (message instanceof Membership) {
            Membership m = (Membership) message;
            processes = m;
        } else if (message instanceof Launch) {
            receiveLaunch();
        } else if (message instanceof Read) {
            receiveRead((Read) message);
        } else if (message instanceof Abort) {
            receiveAbort((Abort) message);
        } else if (message instanceof Gather) {
            receiveGather((Gather) message);
        } else if (message instanceof Impose) {
            receiveImpose((Impose) message);
        } else if (message instanceof Decide) {
            receiveDecide((Decide) message);
        } else if (message instanceof Ack) {
            receiveAck((Ack) message);
        } else if (message instanceof Crash && !crashed) {
            faultProne = true;
        } else if (message instanceof Hold) {
            onHold = true;
        } else {
            unhandled(message);
        }
    }

    public int getId() {
        return i;
    }

    @Override
    public String toString() {
        return "Process #" + i;
    }

}
