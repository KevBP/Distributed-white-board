// Visidia imports
import visidia.simulation.process.algorithm.Algorithm;
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;

// Java imports
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import Message.*;

public class NaimiTrehel extends Algorithm {
    // All nodes data
    private int procId;
    private int owner;
    private int next;
    private boolean token;
    private boolean sc;
    private HashMap<Integer, Integer> doorProcId;

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
        return ("Naimi-Tréhel Algorithm for Mutual Exclusion");
    }

    @Override
    public Object clone() {
        return new NaimiTrehel();
    }

    //
    // Nodes' code
    //
    @Override
    public void init() {
        procId = getId();
        Random rand = new Random(procId);

        doorProcId = new HashMap<>(getNetSize());

        owner = 0;
        next = -1;
        token = false;
        sc = false;
        if(procId == 0) {
            owner = -1;
            token = true;
        }

        rr = new ReceptionRules(this);
        rr.start();

        REQMessage msg = new REQMessage(MsgType.INIT, procId);
        sendAll(msg);

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

    // Rule 0 : init, to know wich door to go to proc
    void initDoor(int d, int p) {
        doorProcId.put(d, p);
    }

    // Rule 1 : ask for critical section
    synchronized void askForCritical() {
        waitForCritical = true;
        sc = true;
        if (owner != -1) {
            REQMessage msg = new REQMessage(procId);
            int dest = getKeyByValue(owner);
            if (dest > -1) {
                sendTo(dest, msg);
                System.out.println(procId + " - Ask for critical to " + owner);
                owner = -1;
                while (token != true) {
                    displayState();
                    try {
                        this.wait();
                    } catch (InterruptedException ie) {
                    }
                }
            }
            else {
                System.out.println(procId + " - Can't ask for critical to " + owner);
            }
        }
    }

    // Rule 2 : Receive REQ from d
    void receiveREQ(int d, int from) {
        if (owner == -1) {
            if (sc == true) {
                next = from;
                System.out.println(procId + " - Receive REQ from " + doorProcId.get(d) + " for " + from + ", added to next");
            }
            else {
                token = false;
                TOKENMessage msg = new TOKENMessage();
                int dest = getKeyByValue(from);
                if (dest > -1) {
                    sendTo(dest, msg);
                    System.out.println(procId + " - Receive REQ from " + doorProcId.get(d) + " for " + from + ", send token");
                }
                else {
                    System.out.println(procId + " - Receive REQ from " + doorProcId.get(d) + " for " + from + ", but can't send token");
                }
            }
        }
        else {
            REQMessage msg = new REQMessage(from);
            int dest = getKeyByValue(owner);
            if (dest > -1) {
                sendTo(dest, msg);
                System.out.println(procId + " - Receive REQ from " + doorProcId.get(d) + " for " + from + ", send REQ to " + owner);
            }
            else {
                System.out.println(procId + " - Receive REQ from " + doorProcId.get(d) + " for " + from + ", can't send REQ to " + owner);
            }
        }
        displayState();
        owner = from;
    }

    // Rule 3 : Receive the TOKEN from d
    synchronized void receiveTOKEN(int d) {
        token = true;
        System.out.println(procId + " - Receive TOKEN from " + doorProcId.get(d));
        notify();
    }

    // Rule 4
    void endCriticalUse() {
        System.out.println(procId + " - End critical use");
        inCritical = false;
        displayState();
        sc = false;
        if (next != -1) {
            TOKENMessage msg = new TOKENMessage();
            int dest = getKeyByValue(next);
            if (dest > -1) {
                sendTo(dest, msg);
                System.out.println(procId + " - Send TOKEN to " + next);
                token = false;
                next = -1;
            }
            else {
                System.out.println(procId + " - Can't send TOKEN to " + next);
            }
        }
    }

    // Access to receive function
    public Message recoit(Door d) {
        return receive(d);
    }

    // Display state
    void displayState() {

        String state = new String("\n");
        state = state + "--------------------------------------\n";
        if (token == true) {
            state += "TOKEN\n";
        }
        if (inCritical)
            state = state + "** ACCESS CRITICAL **";
        else if (waitForCritical)
            state = state + "* WAIT FOR *";
        else
            state = state + "-- SLEEPING --";

        df.display(state);
    }

    private int getKeyByValue(int value) {
        for (Entry<Integer, Integer> e : doorProcId.entrySet()) {
            if(e.getValue() == value) {
                return e.getKey();
            }
        }
        return -1;
    }
}
