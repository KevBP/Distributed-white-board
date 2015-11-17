package lelanngui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import lelann.LeLann;
import lelann.Token;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;


public class LeLannGui extends LeLann<TokenDataTable> implements FormePaintedListener {

    public static final boolean STRICT_DRAWING_ORDER = true;

    private final Object tableauLock = new Object();
    private final TransferQueue<Forme> paintQueue = new LinkedTransferQueue<>();
    private TableauBlancUI tableau;
    private List<Forme> tmpPaintQueue = new ArrayList<>();

    @Override
    public void setup() {
        super.setup();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(LeLannGui.this);
                    tableau.setTitle(String.format("Tableau Blanc de %d", getId()));
                    for (Forme forme : tmpPaintQueue) {
                        tableau.delivreForme(forme);
                    }
                    tmpPaintQueue = null;
                }
            }
        });
    }

    @Override
    public Token<TokenDataTable> initToken() {
        return new Token<>(new TokenDataTable());
    }

    private void paintFormes(Iterable<Forme> formes) {
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

    private void paintForme(Forme forme) {
        paintFormes(Collections.singleton(forme));
    }

    @Override
    public Token criticalSection(Token<TokenDataTable> token) {
        TokenDataTable table = token.getData();
        List<Forme> buff = new ArrayList<>();
        paintQueue.drainTo(buff);
        if (!STRICT_DRAWING_ORDER) {
            removeFormes(buff);
        }
        boolean painted = false;
        for (Integer node : table) {
            if (!painted && node >= getId()) {
                paintFormes(buff);
                painted = true;
            }
            if (node != getId()) {
                List<Forme> formes = table.getFormes(node);
                paintFormes(formes);
            }
        }

        if (!painted) {
            paintFormes(buff);
        }
        if (buff.size() > 0) {
            table.putFormes(getId(), buff);
        } else {
            table.removeFormes(getId());
        }
        return new Token<>(table);
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
    public Object clone() {
        return new LeLannGui();
    }

    @Override
    public void onPaint(Forme forme) {
        paintQueue.add(forme);
        if (STRICT_DRAWING_ORDER) {
            removeForme(forme);
        }
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
