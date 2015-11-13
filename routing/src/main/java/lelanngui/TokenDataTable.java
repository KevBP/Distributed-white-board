package lelanngui;

import gui.Forme;
import utils.SparseArray;

import java.util.*;

public class TokenDataTable implements Iterable<Integer> {
    private final SparseArray<List<Forme>> formesTable;

    public TokenDataTable() {
        formesTable = new SparseArray<>();
    }

    public List<Forme> getFormes(int idx) {
        return Collections.unmodifiableList(formesTable.get(idx));
    }

    private List<Forme> getFormesOrCreate(int idx) {
        List<Forme> formes = formesTable.get(idx);
        if (formes == null) {
            formes = new ArrayList<>();
            formesTable.put(idx, formes);
        }
        return formes;
    }

    public void addForme(int idx, Forme forme) {
        List<Forme> formes = getFormesOrCreate(idx);
        formes.add(forme);
    }

    public void addFormes(int idx, Iterable<Forme> formes) {
        List<Forme> currentFormes = getFormesOrCreate(idx);
        for (Forme forme : formes) {
            currentFormes.add(forme);
        }
    }

    public void putFormes(int idx, Iterable<Forme> formes) {
        List<Forme> currentFormes = getFormesOrCreate(idx);
        currentFormes.clear();
        for (Forme forme : formes) {
            currentFormes.add(forme);
        }
    }

    public void removeFormes(int idx) {
        formesTable.remove(idx);
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
