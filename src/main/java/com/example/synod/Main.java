package com.example.synod;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.example.synod.message.Launch;
import com.example.synod.message.Membership;

import java.util.*;

/**
 * TODO :
 * - Créer les types de messages :
 *    - Read
 *    - Abort
 *    - Gather
 *    - Decide
 *    - Impose
 *    - Ack
 *    - Decide
 * 
 * - Créer les différents handles selon le type de message
 * 
 * - Créer le système de failure
 * 
 * - Faire les tests de fonctionnement
 * 
 * 
 */
 

public class Main {
    public static int N = 3;
    public static float CRASH_PROBABILITY = 0.1f;
    public static void main(String[] args) throws InterruptedException {
        // Instantiate an actor system
        final ActorSystem system = ActorSystem.create("system");
        system.log().info("System started with N=" + N );

        ArrayList<ActorRef> processes = new ArrayList<>();

        for (int i = 0; i < N; i++) {
            final ActorRef a = system.actorOf(Process.createActor(N, i, CRASH_PROBABILITY));
            processes.add(a);
        }

        //give each process a view of all the other processes
        Membership m = new Membership(processes);
        for (ActorRef actor : processes) {
            actor.tell(m, ActorRef.noSender());
        }

        processes.get(0).tell(
                new Launch(),
                ActorRef.noSender());

    }
}
