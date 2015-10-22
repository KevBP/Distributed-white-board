package routing;

import routing.message.Hello;
import routing.message.LinkStateUpdate;
import routing.message.RoutingMessage;
import routing.table.RoutingRecord;
import routing.table.VisidiaRoutingTable;
import visidia.simulation.SimulationAbortError;
import visidia.simulation.process.algorithm.Algorithm;
import visidia.simulation.process.criterion.Criterion;
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;
import visidia.simulation.process.messages.MessagePacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class RoutingAlgo extends Algorithm {

    private VisidiaRoutingTable routingTable;
    private transient ScheduledExecutorService scheduledThreadPool;
    private final transient Set<Integer> linkStateUpdateQueue = new HashSet<>();


    @Override
    public Object clone() {
        //can't call super.clone thank to visidia api
        return new RoutingAlgo();
    }

    @Override
    public void init() {
        getMessageTypeList().add(RoutingMessage.ROUTING_MESSAGE_TYPE); // TODO

        System.out.println("----------------------------------------");
        routingTable = new VisidiaRoutingTable(getNetSize());
        routingTable.updateRoute(getId(), null, 0);
        System.out.println(routingTable);
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

        try {
            Thread.sleep(50_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //scheduledThreadPool.shutdown();
        try {
            scheduledThreadPool.awaitTermination(15, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            scheduledThreadPool.shutdownNow();
        }

    }

    public Message receive(Door door, Criterion criterion) {
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
        synchronized (routingTable) {
            RoutingRecord<Integer, Integer> originalRecord = routingTable.getRecord(from);
            RoutingRecord<Integer, Integer> record = new RoutingRecord<>(door, weight);
            routingTable.updateRoute(from, record);
            return originalRecord == null || !record.equals(originalRecord);
        }
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
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
                        if (message instanceof Hello) {

                            int from = (int) message.getData();
                            if (updateRouteTable(from, door.getNum(), 1)) {
                                synchronized (linkStateUpdateQueue) {
                                    for (int doorNbr = 0; doorNbr < getArity(); doorNbr++) {
                                        if (doorNbr != door.getNum()) {
                                            linkStateUpdateQueue.add(doorNbr);
                                        }
                                    }
                                }
                                scheduledThreadPool.schedule(new SpreadLinkStateUpdate(), 500, TimeUnit.MILLISECONDS);
                            }

                        }
                        if (message instanceof LinkStateUpdate) {
                            VisidiaRoutingTable peerRoutingTable = (VisidiaRoutingTable) message.getData();
                            boolean updated = false;
                            int basePathWeight = 1;
                            for (Integer node : peerRoutingTable) {
                                RoutingRecord<Integer, Integer> currentRecord = routingTable.getRecord(node);
                                Integer currentWeight = currentRecord != null ? currentRecord.getWeight() : null;
                                if (currentWeight == null) {
                                    currentWeight = Integer.MAX_VALUE;
                                }
                                RoutingRecord<Integer, Integer> peerRecord = peerRoutingTable.getRecord(node);
                                Integer peerWeight = peerRecord != null ? peerRecord.getWeight() : null;
                                if (peerWeight != null && basePathWeight + peerWeight < currentWeight &&
                                        updateRouteTable(node, door.getNum(), basePathWeight + peerWeight)) {
                                    updated = true;
                                    synchronized (linkStateUpdateQueue) {
                                        for (int doorNbr = 0; doorNbr < getArity(); doorNbr++) {
                                            if (doorNbr != door.getNum()) {
                                                linkStateUpdateQueue.add(doorNbr);
                                            }
                                        }
                                    }
                                }
                            }
                            if (updated) {
                                scheduledThreadPool.schedule(new SpreadLinkStateUpdate(), 750, TimeUnit.MILLISECONDS);
                            }

                        }
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

    @SuppressWarnings("SynchronizeOnNonFinalField")
    class SpreadLinkStateUpdate implements Runnable {

        @Override
        public void run() {
            List<Integer> buff = new ArrayList<>();
            synchronized (linkStateUpdateQueue) {
                buff.addAll(linkStateUpdateQueue);
                linkStateUpdateQueue.clear();
            }
            for (Integer door : buff) {
                try {
                    sendTo(door, new LinkStateUpdate((VisidiaRoutingTable) routingTable.clone()));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }


        }
    }


}
