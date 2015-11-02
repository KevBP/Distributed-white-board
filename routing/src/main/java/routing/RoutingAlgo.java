package routing;

import routing.message.Hello;
import routing.message.LinkStateUpdate;
import routing.message.RoutingMessage;
import routing.message.SendToMessage;
import routing.message.visitor.Visitor;
import routing.table.RoutingRecord;
import routing.table.VisidiaRoutingTable;
import visidia.simulation.SimulationAbortError;
import visidia.simulation.process.algorithm.Algorithm;
import visidia.simulation.process.criterion.Criterion;
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;
import visidia.simulation.process.messages.MessagePacket;
import visidia.simulation.process.messages.MessageType;

import java.util.*;
import java.util.concurrent.*;

public abstract class RoutingAlgo extends Algorithm implements Visitor {

    private final transient Set<Integer> linkStateUpdateQueue = new HashSet<>();
    private final List<BlockingQueue<Object>> messageQueues = new ArrayList<>();
    private final Object routeTableReadUpdateLock = new Object();
    private VisidiaRoutingTable routingTable;
    private transient ScheduledExecutorService scheduledThreadPool;


    @Override
    public Collection<MessageType> getMessageTypeList() {
        Collection typesList = new LinkedList();
        typesList.add(RoutingMessage.ROUTING_MESSAGE_TYPE);
        return typesList;
    }

    @Override
    public void init() {
        messageQueues.addAll(Collections.nCopies(getNetSize(), (BlockingQueue<Object>) null));
        if (getId() == 0) {
            System.out.println("----------------------------------------");
        }
        routingTable = new VisidiaRoutingTable(getNetSize());
        routingTable.updateRoute(getId(), null, 0);

        scheduledThreadPool = Executors.newScheduledThreadPool(2, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });


        scheduledThreadPool.execute(new RoutingMessageListener());

        //Routing start here

        sendAll(new Hello(getId()));

        //sendToNode(0, "coucou");

        setup();


        try {
            scheduledThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new SimulationAbortError();
        } finally {
            scheduledThreadPool.shutdownNow();
        }
    }


    public BlockingQueue<Object> getQueueForNode(int node) {
        if (node < 0 || node >= getNetSize()) {
            return null; //TODO exception ?
        }
        synchronized (messageQueues) {
            BlockingQueue<Object> queue = messageQueues.get(node);
            if (queue == null) {
                queue = new LinkedBlockingQueue<>();
                messageQueues.set(node, queue);
            }
            return queue;
        }
    }

    public boolean isDataInQueue(int node) {
        synchronized (messageQueues) {
            BlockingQueue<Object> queue = messageQueues.get(node);
            return queue != null && !queue.isEmpty();
        }
    }

    public void sendAllQueueData(int node) {
        BlockingQueue<Object> queue = getQueueForNode(node);
        List<Object> buff = new ArrayList<>();
        queue.drainTo(buff);
        for (Object o : buff) {
            sendToNode(node, o);
        }
    }

    public synchronized Message receive(Door door, Criterion criterion) {
        this.proc.runningControl();
        Message msg;
        try {
            msg = this.proc.getNextMessage(door, criterion);
            return msg;
        } catch (InterruptedException e) {
            throw new SimulationAbortError();
        }
    }

    private boolean updateRouteTable(int from, int door, int weight) {
        synchronized (routeTableReadUpdateLock) {
            RoutingRecord<Integer, Integer> originalRecord = routingTable.getRecord(from);
            RoutingRecord<Integer, Integer> record = new RoutingRecord<>(door, weight);
            routingTable.updateRoute(from, record);
            return originalRecord == null || !record.equals(originalRecord);
        }


    }

    public void sendToNode(int node, Object data) {
        if (node < 0 || node >= getNetSize()) {
            return; //TODO exception ?
        }
        synchronized (routeTableReadUpdateLock) {
            RoutingRecord<Integer, Integer> record = routingTable.getRecord(node);
            if (record == null) {
                getQueueForNode(node).add(data);
            } else if (record.getDoor() == null) { // loopback interface simulation
                SendToMessage message = new SendToMessage(getId(), getId(), data);
                pushSendToMessage(message);
            } else {
                sendTo(record.getDoor(), new SendToMessage(getId(), node, data));
            }
        }
    }


    @Override
    public void visit(Hello message, Door door) {
        int from = (int) message.getData();
        boolean updated = false;
        synchronized (routeTableReadUpdateLock) {
            if (updateRouteTable(from, door.getNum(), 1)) {
                updated = true;
                if (isDataInQueue(from)) {
                    sendAllQueueData(from);
                }
                for (int doorNbr = 0; doorNbr < getArity(); doorNbr++) {
                    if (doorNbr != door.getNum()) {
                        linkStateUpdateQueue.add(doorNbr);
                    }

                }
            }
        }
        if (updated) {
            scheduledThreadPool.schedule(new SpreadLinkStateUpdate(), 750, TimeUnit.MILLISECONDS);
        }
    }


    @Override
    public void visit(LinkStateUpdate message, Door door) {
        VisidiaRoutingTable peerRoutingTable = (VisidiaRoutingTable) message.getData();
        boolean updated = false;
        int basePathWeight = 1;
        synchronized (routeTableReadUpdateLock) {
            for (Integer node : peerRoutingTable) {
                RoutingRecord<Integer, Integer> currentRecord = routingTable.getRecord(node);
                Integer currentWeight = currentRecord != null ? currentRecord.getWeight() : null;
                if (currentWeight == null) {
                    currentWeight = Integer.MAX_VALUE;
                }
                RoutingRecord<Integer, Integer> peerRecord = peerRoutingTable.getRecord(node);
                Integer peerWeight = peerRecord != null ? peerRecord.getWeight() : null;
                if (peerWeight != null && basePathWeight + peerWeight < currentWeight) {
                    if (updateRouteTable(node, door.getNum(), basePathWeight + peerWeight)) {
                        updated = true;
                        if (isDataInQueue(node)) {
                            sendAllQueueData(node);
                        }
                        for (int doorNbr = 0; doorNbr < getArity(); doorNbr++) {
                            if (doorNbr != door.getNum()) {
                                linkStateUpdateQueue.add(doorNbr);
                            }
                        }
                    }
                }
            }
        }
        if (updated) {
            scheduledThreadPool.schedule(new SpreadLinkStateUpdate(), 750, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void visit(final SendToMessage message, Door door) {
        if (message.getTo() == getId()) {
            pushSendToMessage(message);
        } else {
            RoutingRecord<Integer, Integer> record;
            synchronized (routeTableReadUpdateLock) {
                record = routingTable.getRecord(message.getTo());
            }
            if (record == null) {
                throw new RuntimeException("Node " + getId() + " has no route to " + message.getTo());
            }
            int outDoor = record.getDoor();
            sendTo(outDoor, message);
        }
    }

    private void pushSendToMessage(final SendToMessage message) {
        scheduledThreadPool.execute((new Runnable() {
            @Override
            public void run() {
                onMessage(message);
            }
        }));
    }

    public abstract void setup();

    public abstract void onMessage(SendToMessage message);


    class RoutingMessageListener implements Runnable {
        private volatile boolean stop;

        @Override
        public void run() {
            while (!stop) {
                try {
                    Door door = new Door();
                    Criterion isRoutingMessage = o -> o instanceof MessagePacket &&
                            ((MessagePacket) o).message() instanceof RoutingMessage;

                    RoutingMessage message = (RoutingMessage) receive(door, isRoutingMessage);
                    if (message != null) {
                        message.accept(RoutingAlgo.this, door);
                        System.out.printf("i'm %d and i have this table: %s\n", getId(), routingTable);
                    } else {
                        Thread.yield();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class SpreadLinkStateUpdate implements Runnable {

        @Override
        public void run() {
            synchronized (routeTableReadUpdateLock) {
                for (Integer door : linkStateUpdateQueue) {
                    try {
                        sendTo(door, new LinkStateUpdate((VisidiaRoutingTable) routingTable.clone()));
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
                linkStateUpdateQueue.clear();
            }
        }
    }

}
