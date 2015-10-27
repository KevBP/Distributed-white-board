// Visidia imports
import visidia.simulation.process.algorithm.Algorithm;
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;

// Java imports
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import Message.*;

public class RicartAgrawala extends Algorithm {
    // All nodes data
    private int procId;
    private int h;
    private int hSC;
    private int nbProcs;
    private List<Integer> waiting;

    // Higher speed means lower simulation speed
    private int speed = 4;

    // To display the state
    private boolean waitForCritical = false;
    private boolean inCritical = false;

    // Critical section thread
    private ReceptionRules rr = null;
    // State display frame
    private DisplayFrame df;

    public String getDescription() {
        return ("Ricart-Agrawala Algorithm for Mutual Exclusion");
    }

    @Override
    public Object clone() {
        return new RicartAgrawala();
    }

    //
    // Nodes' code
    //
    @Override
    public void init() {
        procId = getId();
        Random rand = new Random(procId);

        h = 0;
        hSC = 0;
        waiting = new LinkedList<Integer>();

        rr = new ReceptionRules(this);
        rr.start();

        // Display initial state + give time to place frames
        df = new DisplayFrame(procId);
        displayState();
        try {
            Thread.sleep(15000);
        } catch (InterruptedException ie) {
        }

        while (true) {
            // Wait for some time
            int time = (3 + rand.nextInt(10)) * speed * 1000;
            System.out.println("Process " + procId + " wait for " + time);
            try {
                Thread.sleep(time);
            } catch (InterruptedException ie) {
            }

            // Try to access critical section
            askForCritical();

            // Access critical
            waitForCritical = false;
            inCritical = true;

            displayState();

            // Simulate critical resource use
            time = (1 + rand.nextInt(3)) * 1000;
            System.out.println("Process " + procId + " enter SC " + time);
            try {
                Thread.sleep(time);
            } catch (InterruptedException ie) {
            }
            System.out.println("Process " + procId + " exit SC ");

            // Release critical use
            endCriticalUse();
        }
    }

    //--------------------
    // Rules
    //-------------------

    // Rule 1 : ask for critical section
    synchronized void askForCritical() {
        waitForCritical = true;
        hSC = h + 1;
        nbProcs = getArity();
        REQMessage msg = new REQMessage(MsgType.REQ, hSC);
        sendAll(msg);
        System.out.println(procId + " - Ask for critical to all");
        while (nbProcs != 0) {
            displayState();
            try {
                this.wait();
            } catch( InterruptedException ie) {}
        }
    }

    // Rule 2 : receive REQ from d
    void receiveREQ(int d, int hd) {
        h = Math.max(h, hd);
        if (waitForCritical && ((hSC < hd) || ((hSC == hd) && procId < d))) {
            waiting.add(d);
            System.out.println(procId + " - Receive REQ from " + d + ", add to waiting list " + d);
        }
        else {
            RELMessage msg = new RELMessage(MsgType.REL);
            sendTo(d, msg);
            System.out.println(procId + " - Receive REQ from " + d + ", send REL to " + d);
        }
    }

    // Rule 3 : receive REL from d
    synchronized void receiveREL(int d) {
        nbProcs--;
        System.out.println(procId + " - ReceiveREL from " + d + ", nbProcs = " + nbProcs);
        if (nbProcs == 0) {
            notify();
        }
    }

    // Rule 4
    void endCriticalUse() {
        inCritical = false;
        RELMessage msg = new RELMessage(MsgType.REL);
        for (int node : waiting) {
            sendTo(node, msg);
        }
        waiting.clear();
        displayState();
    }

    // Access to receive function
    public Message recoit(Door d) {
        return receive(d);
    }

    // Display state
    void displayState() {

        String state = new String("\n");
        state = state + "--------------------------------------\n";
        if (inCritical)
            state = state + "** ACCESS CRITICAL **";
        else if (waitForCritical)
            state = state + "* WAIT FOR *";
        else
            state = state + "-- SLEEPING --";

        df.display(state);
    }
}
