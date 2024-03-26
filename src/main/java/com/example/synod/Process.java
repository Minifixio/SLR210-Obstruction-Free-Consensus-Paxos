package com.example.synod;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;

import com.example.synod.message.*;

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

    private void broadcast(Message message) {
        for (ActorRef actor : processes.references) {
            if (actor != getSelf())
                actor.tell(message, getSelf());
        }
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

    private void propose(Boolean v) {

        if (onHold) {
            return;
        }

        if (debug)
            log.info(this + " - propose(" + v + ")");

        proposal = v;
        ballot += n;
        initState();
        broadcast(new Read(ballot));
    }

    private void receiveRead(Read message) {
        if (debug)
            log.info(this + " - read received");
        int newBallot = message.getBallot();
        if (newBallot < readballot || imposeballot > newBallot) {
            getSender().tell(new Abort(newBallot), getSelf());
        } else {
            readballot = newBallot;
            getSender().tell(new Gather(newBallot, imposeballot, estimate, i), getSelf());
        }

    }

    // TODO : check if the implementation is right, proposing the same value after getting abort
    private void receiveAbort(Abort message) {
        if (debug)
            log.info(this + " - abort received");
        propose(proposal);
    }

    private void receiveGather(Gather message) {
        if (debug)
            log.info(this + " - gather received");
        states.set(message.getSenderId(),
                new Pair<Boolean, Integer>(message.getEstimate(), message.getEstimateBallot()));
        if (debug)
            log.info(this + " - states : " + states);

        // check if the process has received enough messages
        int count = 0;
        // count the non-null states
        for (Pair<Boolean, Integer> state : states) {
            if (state != null) {
                count++;
            }
        }
        if (count > n / 2) {
            if (debug)
                log.info(this + " - received enough messages");
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
            broadcast(new Impose(ballot, proposal, i));
        }
    }

    private void receiveImpose(Impose message) {
        if (debug)
            log.info(this + " - impose received");
        int newBallot = message.getBallot();
        if (readballot > newBallot || imposeballot > newBallot) {
            getSender().tell(new Abort(newBallot), getSelf());
        } else {
            estimate = message.getProposal();
            imposeballot = newBallot;
            getSender().tell(new Ack(newBallot), getSelf());
        }
    }

    private void receiveDecide(Decide message) {

        
        decided = true;

        // send a Decide message to all processes
        broadcast(message);
        log.info(this + " - decided " + message.getProposal() + " in " + (System.currentTimeMillis() - initTime) + "ms");
    }

    private void receiveAck(Ack message) {
        // if majority of ack received, send a Decide message to all processes
        ackReceived++;

        if (ackReceived > n / 2) {
            ackReceived = 0;
            if (debug)
                log.info(this + " - majority of ack received");
            decided = true;

            // TODO : stop execution and output the time (System.currentTimeMillis() - initTime) to the main or a file

            log.info(this + " - decided " + proposal + " in " + (System.currentTimeMillis() - initTime) + "ms");


            Decide decide = new Decide(proposal);
            broadcast(decide);
        }
    }

    private void receiveLaunch() {
        if (debug)
            log.info(this + " - launch received");
        // pick a random value and propose it
        Random rand = new Random();
        Boolean v = rand.nextBoolean();
        propose(v);
    }

    private void crash() {
        if (Math.random() < crashProbability) {
            if (debug)
                log.info(this + " - CRASHED");
            crashed = true;
        }
    }


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
