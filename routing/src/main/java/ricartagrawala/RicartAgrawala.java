package ricartagrawala;

import gui.Forme;
import gui.FormePaintedListener;
import gui.facade.TableauBlanc;
import gui.facade.TableauBlancImpl;
import ricartagrawala.message.DataMessage;
import ricartagrawala.message.RELMessage;
import ricartagrawala.message.REQMessage;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RicartAgrawala extends RoutingAlgo implements FormePaintedListener {
    public static final boolean STRICT_DRAWING_ORDER = true;

    protected final Object criticalSectionLock = new Object();
    private final Queue<Forme> myPaintQueue = new LinkedList<>();
    private final AtomicBoolean waitForCritical = new AtomicBoolean();
    private final AtomicInteger waitingRelCount = new AtomicInteger(1);
    private final List<Integer> waitingNodes = new ArrayList<>();
    private int h;
    private int hSC;
    private TableauBlanc tableau;

    @Override
    public void setup() {
        h = 0;
        hSC = 0;
        tableau = new TableauBlancImpl(String.format("Tableau Blanc de %d", getId()), RicartAgrawala.this);
    }

    public boolean requestCriticalSectionNow() {
        if (waitForCritical.compareAndSet(false, true) && waitingRelCount.compareAndSet(1, getNetSize())) {
            hSC = h + 1;
            sendToAllNode(new REQMessage(hSC));
            return true;
        }
        return false;
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 2
            REQMessage reqMessage = (REQMessage) message.getData();
            int hd = reqMessage.getH();
            h = Math.max(hd, h);
            boolean await = false;
            synchronized (waitingNodes) {
                if (waitForCritical.get() && ((hSC < hd) || ((hSC == hd) && getId() < message.getFrom()))) {
                    waitingNodes.add(message.getFrom());
                    await = true;
                }
            }
            if (!await) {
                sendToNode(message.getFrom(), new RELMessage());
            }
        } else if (message.getData() instanceof RELMessage) { // Rule 3
            if (waitingRelCount.decrementAndGet() == 1) {
                criticalSection();
                synchronized (waitingNodes) {
                    for (Integer node : waitingNodes) {
                        sendToNode(node, new RELMessage());
                    }
                    waitForCritical.set(false);
                }
            }
        } else if (message.getData() instanceof DataMessage) {
            @SuppressWarnings("unchecked")
            List<Forme> formes = ((DataMessage<List<Forme>>) message.getData()).getData();
            if (!isStrictDrawingOrder()) {
                tableau.removeFormes(formes);
            }
            tableau.paintFormes(formes);
        } else {
            throw new RuntimeException("Wrong type message");
        }
    }

    public void criticalSection() {
        synchronized (criticalSectionLock) {
            DataMessage<ArrayList<Forme>> dataMessage;
            synchronized (myPaintQueue) {
                dataMessage = new DataMessage<>(new ArrayList<>(myPaintQueue));
                myPaintQueue.clear();
            }
            sendToAllNode(dataMessage, true);
        }
    }

    public boolean isStrictDrawingOrder() {
        return STRICT_DRAWING_ORDER;
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
        if (isStrictDrawingOrder()) {
            tableau.removeForme(forme);
        }

        requestCriticalSectionNow();
    }

    @Override
    public void onExit() {
        tableau.exit();
        super.onExit();
    }
}
