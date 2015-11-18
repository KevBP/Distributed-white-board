package gui.facade;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class TableauBlancImpl implements TableauBlanc {
    private final Object tableauLock = new Object();
    private TableauBlancUI tableau;
    private List<Forme> tmpPaintQueue = new ArrayList<>();
    private String title;
    private FormePaintedListener listener;


    public TableauBlancImpl(String title, FormePaintedListener listener) {
        setTitle(title);
        setFormePaintedListener(listener);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(null);
                    if (TableauBlancImpl.this.title != null) {
                        tableau.setTitle(TableauBlancImpl.this.title);
                    }
                    tableau.setFormePaintedListener(TableauBlancImpl.this.listener);
                    for (Forme forme : tmpPaintQueue) {
                        tableau.delivreForme(forme);
                    }
                    tmpPaintQueue = null;
                }
            }
        });
    }

    public TableauBlancImpl(FormePaintedListener listener) {
        this(null, listener);
    }

    public TableauBlancImpl(String title) {
        this(title, null);
    }

    public TableauBlancImpl() {
        this(null, null);
    }

    @Override
    public void paintForme(Forme forme) {
        paintFormes(Collections.singleton(forme));
    }

    @Override
    public void paintFormes(Iterable<Forme> formes) {
        synchronized (tableauLock) {
            for (Forme forme : formes) {
                if (forme == null) {
                    continue;
                }
                if (tableau == null) {
                    tmpPaintQueue.add(forme);
                } else {
                    tableau.delivreForme(forme);
                }
            }
        }

    }

    public void removeForme(Forme forme) {
        removeFormes(Collections.singleton(forme));
    }

    public void removeFormes(Iterable<Forme> formes) {
        synchronized (tableauLock) {
            for (Forme forme : formes) {
                tableau.removeLastForme(forme);
            }
        }
    }

    @Override
    public void exit() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    if (tableau != null) {
                        tableau.dispose();
                    }
                }
            }
        });
    }

    @Override
    public void setFormePaintedListener(FormePaintedListener listener) {
        this.listener = listener;
        synchronized (tableauLock) {
            if (tableau != null) {
                tableau.setFormePaintedListener(listener);
            }
        }
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
        if (title != null) {
            synchronized (tableauLock) {
                if (tableau != null) {
                    tableau.setTitle(title);
                }
            }
        }
    }
}
