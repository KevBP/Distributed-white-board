package lelanngui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import lelann.LeLann;
import lelann.Token;

import javax.swing.*;
import java.util.*;

/**
 * Created by maxime on 02/11/2015.
 */
public class LeLannGui extends LeLann<List<Forme>> implements FormePaintedListener {
    private final Object tableauLock = new Object();
    private final Queue<Forme> myPaintQueue = new LinkedList<>();
    private TableauBlancUI tableau;
    private List<Forme> toPaintQueue = new ArrayList<>();

    @Override
    public void setup() {
        super.setup();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(LeLannGui.this);
                    for (Forme forme : toPaintQueue) {
                        tableau.delivreForme(forme);
                    }
                    toPaintQueue = null;
                }
            }
        });
    }

    @Override
    public Token<List<Forme>> initToken() {
        ArrayList<Forme> formes = new ArrayList<>(Collections.nCopies(getNetSize(), (Forme) null));
        return new Token<>(formes);
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
    public Token criticalSection(Token<List<Forme>> token) {
        List<Forme> formes = token.getData();
        formes.set(getId(), null);
        for (Forme forme : formes) {
            paintForme(forme);
        }
        formes.set(getId(), myPaintQueue.poll());
        return new Token<>(formes);
    }

    @Override
    public Object clone() {
        return new LeLannGui();
    }

    @Override
    public void onPaint(Forme forme) {
        myPaintQueue.add(forme);
    }
}
