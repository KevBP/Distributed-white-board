package lelanngui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import lelann.LeLann;
import lelann.Token;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;


public class LeLannGui extends LeLann<TokenDataTable> implements FormePaintedListener {
    /**
     * Les formes peintes par l'utilisateurs.
     * <br/> Cette queue est vidée lorsque le noeud a le token
     */
    private final TransferQueue<Forme> paintQueue = new LinkedTransferQueue<>();
    /**
     * Un objet pour faire de la synchronisation externe sur les opérations qui manipulent le tableau.
     *
     * @see #tableau
     */
    private final Object tableauLock = new Object();
    /**
     * L'instance du tableau.
     * <br/> Initialiser en asynchrone.
     */
    private TableauBlancUI tableau;
    /**
     * Queue des formes a peindre lorsque le tableau sera initialisé.
     * <br/> Cette queue est vidée et supprimée lorsque le {@link #tableau} est initialisé
     */
    private List<Forme> tmpPaintQueue = new ArrayList<>();

    @Override
    public void setup() {
        super.setup();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(LeLannGui.this);
                    tableau.setTitle(String.format("Tableau Blanc de %d", getId()));
                    // on vide la tmpPaintQueue
                    for (Forme forme : tmpPaintQueue) {
                        tableau.delivreForme(forme);
                    }
                    // on supprime la tmpPaintQueue
                    tmpPaintQueue = null;
                }
            }
        });
    }

    @Override
    public Token<TokenDataTable> initToken() {
        return new Token<>(new TokenDataTable());
    }

    /**
     * Dessine la forme passée en parametre sur le tableau.
     * <br/> La forme peut etre <code>null</code>
     * <br/> Si le {@link #tableau} n'est pas initialisé, la forme est mise dans la file d'attente {@link #tmpPaintQueue}
     * @param forme la forme a dessinée
     */
    private void paintForme(Forme forme) {
        if (forme == null) {
            return;
        }
        synchronized (tableauLock) {
            if (tableau == null) {
                tmpPaintQueue.add(forme);
            } else {
                tableau.delivreForme(forme);
            }
        }
    }

    @Override
    public Token criticalSection(Token<TokenDataTable> token) {
        TokenDataTable table = token.getData();
        //on peint toutes les formes contenues dans le token
        for (Integer node : table) {
            List<Forme> formes = table.getFormes(node);
            for (Forme forme : formes) {
                paintForme(forme);
            }
        }

        // on vide la notre paintQueue
        List<Forme> buff = new ArrayList<>();
        paintQueue.drainTo(buff);
        // On met nos formes dans le token
        if (buff.size() > 0) {
            table.putFormes(getId(), buff);
        } else {
            table.removeFormes(getId());
        }
        return new Token<>(table);
    }

    @Override
    public Object clone() {
        return new LeLannGui();
    }

    @Override
    public void onPaint(Forme forme) {
        paintQueue.add(forme);
    }

    @Override
    public void onExit() {
        // ferme la fenetre du tableau blanc
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
