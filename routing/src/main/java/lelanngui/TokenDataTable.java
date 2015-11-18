package lelanngui;

import gui.Forme;
import utils.SparseArray;

import java.util.*;

public class TokenDataTable implements Iterable<Integer> {
    private final SparseArray<List<Forme>> formesTable;

    public TokenDataTable() {
        formesTable = new SparseArray<>();
    }

    public List<Forme> getFormes(int node) {
        return Collections.unmodifiableList(formesTable.get(node));
    }

    private List<Forme> getFormesOrCreate(int node) {
        List<Forme> formes = formesTable.get(node);
        if (formes == null) {
            formes = new ArrayList<>();
            formesTable.put(node, formes);
        }
        return formes;
    }

    public void addForme(int node, Forme forme) {
        List<Forme> formes = getFormesOrCreate(node);
        formes.add(forme);
    }

    public void addFormes(int node, Iterable<Forme> formes) {
        List<Forme> currentFormes = getFormesOrCreate(node);
        for (Forme forme : formes) {
            currentFormes.add(forme);
        }
    }

    public void putFormes(int node, Iterable<Forme> formes) {
        List<Forme> currentFormes = getFormesOrCreate(node);
        currentFormes.clear();
        for (Forme forme : formes) {
            currentFormes.add(forme);
        }
    }

    public int getIndex(int node) {
        return formesTable.indexOfKey(node);
    }

    public void removeFormes(int node) {
        formesTable.remove(node);
    }

    @Override
    public Iterator<Integer> iterator() {
        return new AbstractList<Integer>() {
            @Override
            public int size() {
                return formesTable.getSize();
            }

            @Override
            public Integer get(int index) {
                return formesTable.keyAt(index);
            }
        }.iterator();
    }

    @Override
    public String toString() {
        return "TokenDataTable{" +
                formesTable +
                '}';
    }
}
