package naimitrehelgui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.facade.TableauBlanc;
import gui.facade.TableauBlancImpl;
import naimitrehelgui.message.REQMessage;
import naimitrehelgui.message.Token;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class NaimiTrehelGui extends RoutingAlgo implements FormePaintedListener {
    private final Object tokenLock = new Object();
    private final TransferQueue<Forme> paintQueue = new LinkedTransferQueue<>();
    private int owner;
    private boolean sc;
    private boolean token;
    private boolean waitForToken;
    private int next;
    private TableauBlanc tableau;

    @Override
    public void setup() {
        // Rule 1
        owner = 0;
        next = -1;
        sc = false;
        synchronized (tokenLock) {
            token = false;
        }
        waitForToken = false;
        if (getId() == 0) {
            sendToNode(0, new Token());
            synchronized (tokenLock) {
                token = true;
            }
            owner = -1;
        }
        tableau = new TableauBlancImpl(String.format("Tableau Blanc de %d", getId()), this);
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 3
            if (owner == -1) {
                if (sc == true && next == -1) {
                    next = ((REQMessage) message.getData()).getFrom();
                }
                else {
                    synchronized (tokenLock) {
                        token = false;
                    }
                    sendToNode(((REQMessage) message.getData()).getFrom(), new Token());
                }
            }
            else {
                sendToNode(owner, new REQMessage(((REQMessage) message.getData()).getFrom()));
            }
            owner = ((REQMessage) message.getData()).getFrom();
        }
        else if (message.getData() instanceof Token) { // Rule 4
            synchronized (tokenLock) {
                token = true;
                waitForToken = false;
                owner = -1;
                tokenLock.notify();
            }
        }
        else if(message.getData() instanceof TransferQueue) {
            tableau.paintFormes((TransferQueue<Forme>) message.getData());
        }
    }

    public void criticalSection() {
        TransferQueue<Forme> paintQueueToSend = new LinkedTransferQueue<>(paintQueue);
        paintQueue.clear();
        sendToAllNode(paintQueueToSend);
        tableau.removeFormes(paintQueueToSend);
        tableau.paintFormes(paintQueueToSend);
        endCriticalUse();
    }

    // Rule 5
    public void endCriticalUse() {
        sc = false;
        if (next > -1) {
            sendToNode(next, new Token());
            synchronized (tokenLock) {
                token = false;
            }
            next = -1;
        }
    }

    @Override
    public void onPaint(Forme forme) {
        paintQueue.add(forme);
        tableau.removeForme(forme);
        // Rule 2
        sc = true;
        if(owner > -1) {
            if(waitForToken == false) {
                sendToNode(owner, new REQMessage(getId()));
                owner = -1;
                waitForToken = true;
                synchronized (tokenLock) {
                    while (token != true) {
                        try {
                            tokenLock.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        if(owner == -1 && waitForToken == false) {
            criticalSection();
        }
    }

    @Override
    public void onExit() {
        tableau.exit();
        super.onExit();
    }

    @Override
    public Object clone() {
        return new NaimiTrehelGui();
    }
}
