package com.example.synod;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import com.example.synod.message.*;

import scala.concurrent.duration.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * TODO :
 * - Automatiser l'execution
 * - Ecrire le readme
 * - Faire le rapport
 */
 

public class Main {

    public static int N;
    public static float alpha;
    public static int TLE;
    public static int f;
    public static boolean debug;

    public static void main(String[] args) throws InterruptedException, TimeoutException {
        if (args.length >= 3) {
            try {
                N = Integer.parseInt(args[0]);
                alpha = Float.parseFloat(args[1]);
                TLE = Integer.parseInt(args[2]);
                f = N%2 == 0 ? N / 2 - 1: N/2;
                if (args.length >= 4) {
                    debug = Boolean.parseBoolean(args[3]);
                } else {
                    debug = false;
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid input. Usage 'Main N:int alpha:float Tle:int [debug:boolean = false]'");
                System.exit(1);
            }
        } else {
            System.err.println("Insufficient arguments. Please provide 3 arguments. Usage 'Main N:int alpha:float Tle:int [debug = true]'");
            System.exit(1);
        }
        
        if (alpha < 0. || alpha > 1.) {
            System.err.println("Invalid input. Use 0 <= alpha <= 1. Value entered: " + alpha);
            System.exit(1);
        }


        // Instantiate an actor system
        final ActorSystem system = ActorSystem.create("system");
        system.log().info("System started");

        System.out.println("\n\n");
        System.out.println("---");
        System.out.println("System size: " + N + ", alpha: " + alpha + ", TLE: " + TLE);
        System.out.println("---");

        // Create processes and give each process a view of all the other processes
        ArrayList<ActorRef> processes = new ArrayList<>();
        for (int j = 0; j < N; j++) {
            final ActorRef a = system.actorOf(Process.createActor(N, j, alpha, debug));
            processes.add(a);
        }

        // Send Membership message to all processes
        Membership m = new Membership(processes);
        for (ActorRef actor : processes) {
            actor.tell(m, ActorRef.noSender());
        }

        // Send init message to all processes with the current time
        long initTime = System.currentTimeMillis();
        for (ActorRef actor : processes) {
            actor.tell(new Init(initTime), ActorRef.noSender());
        }

        Collections.shuffle(processes);

        // Assume that the first process is the leader
        ActorRef leader = processes.get(0);
        System.out.println("The leader is process " + leader.path().toString());

        // Send a hold message in tle to all processes except the leader
        // We start at j = 1 because the leader is the first process
        // Sending a message to the leader for logs
        system.scheduler().scheduleOnce(Duration.create(TLE, TimeUnit.MILLISECONDS), leader, new Leader(), system.dispatcher(), null);
        for (int j = 1; j < N; j++) {
            system.scheduler().scheduleOnce(Duration.create(TLE, TimeUnit.MILLISECONDS), processes.get(j), new Hold(), system.dispatcher(), null);
        }

        // Crash the f last processes
        for (int j = N - f; j < N; j++) {
            processes.get(j).tell(new Crash(), ActorRef.noSender());
        }



        // Send Launch message to all processes
        for (ActorRef actor : processes) {
            actor.tell(new Launch(), ActorRef.noSender());
        }


        // Terminate the actor system
        // system.terminate();
    }

}