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
 * - Faire les tests de fonctionnement
 * - Dans le main attendre la fin d'une execution avant d'en lancer une autre ou enlever les boulces for et préciser les params dans args
 * voir readme.md!
 */
 

public class Main {

    public static int N = 100;
    public static float alpha = 0f;
    public static int TLE = 500;
    public static int f = 49;

    public static void main(String[] args) throws InterruptedException, TimeoutException {
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
            final ActorRef a = system.actorOf(Process.createActor(N, j, alpha, false));
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

        // Crash the f last processes
        for (int j = N - f; j < N; j++) {
            processes.get(j).tell(new Crash(), ActorRef.noSender());
        }

        // Send Launch message to all processes
        for (ActorRef actor : processes) {
            actor.tell(new Launch(), ActorRef.noSender());
        }

        // Send a hold message in tle to all processes except the leader
        // We start at j = 1 because the leader is the first process
        // Sending a message to the leader for logs
        system.scheduler().scheduleOnce(Duration.create(TLE, TimeUnit.MILLISECONDS), leader, new Leader(), system.dispatcher(), null);
        for (int j = 1; j < N; j++) {
            system.scheduler().scheduleOnce(Duration.create(TLE, TimeUnit.MILLISECONDS), processes.get(j), new Hold(), system.dispatcher(), null);
        }
       
        // Terminate the actor system
        // system.terminate();
    }

}