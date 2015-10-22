package routing.table;


import java.io.Serializable;
import java.util.*;

public class VisidiaRoutingTable implements RoutingTableInterface<Integer, Integer, Integer>, Serializable, Cloneable {
    /**
     *
     */
    private final List<RoutingRecord<Integer, Integer>> table;
    private final int size;

    public VisidiaRoutingTable(int size) {
        this.size = size;
        this.table = new ArrayList<>(Collections.nCopies(size, null));
    }
    @Override
    public synchronized RoutingRecord<Integer, Integer> getRecord(Integer dest){
        return table.get(dest);
    }


    @Override
    public synchronized RoutingRecord<Integer, Integer> updateRoute(Integer dest,  RoutingRecord<Integer, Integer> record) {
        if (dest >= getSize()){
            throw new ArrayIndexOutOfBoundsException();
        }
        return table.set(dest, record);
    }

    public RoutingRecord<Integer, Integer> updateRoute(Integer dest,  Integer door, Integer weigth) {
        return updateRoute(dest, new RoutingRecord<>(door, weigth));
    }

    @Override
    public int getSize() {
        return size;
    }

    public synchronized int getRoutingSize() {
        int ret = 0;
        for (RoutingRecord<Integer, Integer> record : table) {
            if (record != null) {
                ret += 1;
            }
        }
        return ret;
    }

    @Override
    public Iterator<Integer> iterator() {
        return (new AbstractList<Integer>() {
            @Override
            public int size() {
                return size;
            }

            @Override
            public Integer get(int index) {
                return index;
            }
        }).iterator();
    }

    @Override
    public String toString() {
        return "VisidiaRoutingTable{" +
                "table=" + table +
                ", size=" + size +
                '}';
    }

    @Override
    public synchronized Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
