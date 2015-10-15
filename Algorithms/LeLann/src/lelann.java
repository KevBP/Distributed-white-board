import visidia.simulation.process.algorithm.Algorithm;
import visidia.simulation.process.messages.Door;

public class lelann extends Algorithm {
    // Variables du processus
    boolean token = false;
    boolean SC = false;
    int succ;

    // Pour l'affichage
    int procId;
    boolean enSC = false;
    DisplayFrame df;

    // Reception thread
    ReceptionRules rr = null;

    @Override
    public Object clone() {
        return new lelann();
    }

    @Override
    public void init() {
        procId = getId();

        rr = new ReceptionRules(this);
        rr.start();

        if (procId == 0) {
            token = true;
            Door d = new Door();
            succ = d.getNum();
        }

        df = new DisplayFrame(procId);
        displayState();

        Thread thread = new Thread(this);

        while (true) {
            // TODO
        }
    }

    // TODO rules
    // Rule 2
    synchronized void receiveTOKEN(int p, int d){
        System.out.println("Process " + procId + " reveiced REQ from " + p);
        if (SC) {
            token = true;
        }
        else {
            SyncMessage sm = new SyncMessage(MsgType.TOKEN, procId);
            sendTo(succ, sm);
        }
        displayState();
    }

    public SyncMessage recoit ( Door d ) {
        SyncMessage sm = (SyncMessage)receive(d);
        return sm;
    }

    void displayState() {
        String state = new String("--------------------------------------\n");
        if (token) {
            state = state + "[TOKEN]\n";
        }
        if (SC) {
            state = state + "Wainting access critical\n";
        }
        if (enSC) {
            state = state + "Access critical\n";
        }
        state = state + "--------------------------------------\n";
        df.display(state);
    }
}
