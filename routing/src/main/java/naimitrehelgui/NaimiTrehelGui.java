package naimitrehelgui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import naimitrehelgui.message.REQMessage;
import naimitrehelgui.message.Token;
import naimitrehelgui.message.PaintOK;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

import javax.swing.*;

public class NaimiTrehelGui extends RoutingAlgo implements FormePaintedListener {
    private int owner;
    private boolean sc;
    private boolean token;
    private int next;
    private final Object tokenLock = new Object();
    private final Object tableauLock = new Object();
    private TableauBlancUI tableau;
    private Forme forme;
    private int cptPaint;

    @Override
    public void setup() {
        // Rule 1
        owner = 0;
        next = -1;
        sc = false;
        token = false;
        cptPaint = 0;
        if (getId() == 0) {
            sendToNode(0, new Token());
            token = true;
            owner = -1;
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(NaimiTrehelGui.this);
                    tableau.setTitle(String.format("Tableau Blanc de %d", getId()));
                }
            }
        });
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 3
            if (owner == -1) {
                if (sc == true) {
                    next = message.getFrom();
                }
                else {
                    token = false;
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
                owner = -1;
                tokenLock.notify();
            }
        }
        else if(message.getData() instanceof Forme) {
            synchronized (tableauLock) {
                paintForme((Forme) message.getData());
            }
            sendToNode(message.getFrom(), new PaintOK());
        }
        else if(message.getData() instanceof PaintOK) {
            cptPaint++;
            if(cptPaint == getNetSize()-1 && sc == true) {
                endCriticalUse();
            }
        }
    }

    public void criticalSection() {
        cptPaint = 0;
        sendToAllNode(forme);
    }

    // Rule 5
    public void endCriticalUse() {
        sc = false;
        if (next > -1) {
            sendToNode(next, new Token());
            token = false;
            next = -1;
        }
    }

    private void paintForme(Forme forme) {
        if (forme == null) {
            return;
        }
        synchronized (tableauLock) {
            if (tableau == null) {
                //tmpPaintQueue.add(forme);
            } else {
                tableau.delivreForme(forme);
            }
        }
    }

    @Override
    public void onPaint(Forme forme) {
        this.forme = forme;
        // Rule 2
        sc = true;
        if(owner > -1) {
            sendToNode(owner, new REQMessage(getId()));
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
        criticalSection();
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
