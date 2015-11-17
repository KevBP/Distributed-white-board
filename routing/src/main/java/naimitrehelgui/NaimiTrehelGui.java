package naimitrehelgui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import naimitrehelgui.message.REQMessage;
import naimitrehelgui.message.Token;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

public class NaimiTrehelGui extends RoutingAlgo implements FormePaintedListener {
    private int owner;
    private boolean sc;
    private boolean token;
    private boolean waitForToken;
    private int next;
    private final Object tokenLock = new Object();
    private final Object tableauLock = new Object();
    private TableauBlancUI tableau;
    private List<Forme> tmpPaintQueue = new ArrayList<>();
    private final TransferQueue<Forme> paintQueue = new LinkedTransferQueue<>();

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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(NaimiTrehelGui.this);
                    tableau.setTitle(String.format("Tableau Blanc de %d", getId()));
                    for (Forme forme : tmpPaintQueue) {
                        tableau.delivreForme(forme);
                    }
                    tmpPaintQueue = null;
                }
            }
        });
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
            synchronized (tableauLock) {
                for (Forme forme : (TransferQueue<Forme>) message.getData()) {
                    paintForme(forme);
                }
            }
        }
    }

    public void criticalSection() {
        TransferQueue<Forme> paintQueueToSend = new LinkedTransferQueue<>(paintQueue);
        paintQueue.clear();
        sendToAllNode(paintQueueToSend);
        synchronized (tableauLock) {
            for (Forme forme : paintQueueToSend) {
                paintForme(forme);
            }
        }
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

    private void paintForme(Forme forme) {
        if (forme == null) {
            return;
        }
        synchronized (tableauLock) {
            if (tableau == null) {
                tmpPaintQueue.add(forme);
            } else {
                tableau.delivreForme(forme);
            }
        }
    }

    @Override
    public void onPaint(Forme forme) {
        paintQueue.add(forme);
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
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    if (tableau != null) {
                        tableau.dispose();
                    }
                }
            }
        });
        super.onExit();
    }

    @Override
    public Object clone() {
        return new NaimiTrehelGui();
    }
}
