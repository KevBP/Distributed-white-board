package ricartagrawala;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import ricartagrawala.message.DataMessage;
import ricartagrawala.message.RELMessage;
import ricartagrawala.message.REQMessage;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RicartAgrawala extends RoutingAlgo implements FormePaintedListener {
    protected final Object criticalSectionLock = new Object();
    private final Queue<Forme> myPaintQueue = new LinkedList<>();
    private final Object tableauLock = new Object();
    private final AtomicBoolean waitForCritical = new AtomicBoolean();
    private final AtomicInteger waitingRelCount = new AtomicInteger(1);
    private int h;
    private int hSC;
    private List<Integer> waitingNode;
    private TableauBlancUI tableau;
    private List<Forme> toPaintQueue = new ArrayList<>();

    @Override
    public void setup() {
        h = 0;
        hSC = 0;
        waitingNode = new ArrayList<>();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(RicartAgrawala.this);
                    tableau.setTitle(String.format("Tableau Blanc de %d", getId()));
                    for (Forme forme : toPaintQueue) {
                        tableau.delivreForme(forme);
                    }
                    toPaintQueue = null;
                }
            }
        });
    }

    public boolean requestCriticalSectionNow() {
        if (waitForCritical.compareAndSet(false, true) && waitingRelCount.compareAndSet(1, getNetSize())) {
            hSC = h + 1;
            sendToAllNode(new REQMessage(hSC));
            return true;
        }
        return false;
    }

    private void paintForme(Forme forme) {
        if (forme == null) {
            return;
        }
        synchronized (tableauLock) {
            if (tableau == null) {
                toPaintQueue.add(forme);
            } else {
                tableau.delivreForme(forme);
            }
        }
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 2
            REQMessage reqMessage = (REQMessage) message.getData();
            int hd = reqMessage.getH();
            h = Math.max(hd, h);
            if (waitForCritical.get() && ((hSC < hd) || ((hSC == hd) && getId() < message.getFrom()))) {
                waitingNode.add(message.getFrom());
            }
            else {
                sendToNode(message.getFrom(), new RELMessage());
            }
        }
        else if (message.getData() instanceof  RELMessage) { // Rule 3
            if (waitingRelCount.decrementAndGet() == 1) {
                criticalSection();
                for (Integer node : waitingNode) {
                    sendToNode(node, new RELMessage());
                }
                waitForCritical.set(false);
            }
        } else if (message.getData() instanceof DataMessage) {
            @SuppressWarnings("unchecked")
            List<Forme> formes = ((DataMessage<List<Forme>>) message.getData()).getData();
            for (Forme forme : formes) {
                paintForme(forme);
            }
        }
        else {
            throw new RuntimeException("Wrong type message");
        }
    }

    public void criticalSection() {
        synchronized (criticalSectionLock) {
            synchronized (myPaintQueue) {
                sendToAllNode(new DataMessage<>(new ArrayList<>(myPaintQueue)), true);
                myPaintQueue.clear();
            }
        }
    }

    @Override
    public Object clone() {
        return new RicartAgrawala();
    }

    @Override
    public void onPaint(Forme forme) {
        synchronized (myPaintQueue) {
            myPaintQueue.add(forme);
        }
        requestCriticalSectionNow();
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
}
