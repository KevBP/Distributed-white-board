package naimitrehelgui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import naimitrehel.Naimi_Trehel;
import naimitrehel.message.REQMessage;
import naimitrehel.message.Token;
import naimitrehelgui.message.PaintOK;
import routing.message.SendToMessage;

import javax.swing.*;

public class NaimiTrehelGui extends Naimi_Trehel implements FormePaintedListener {
    private final Object tableauLock = new Object();
    private TableauBlancUI tableau;
    private Forme forme;
    private int cptPaint;

    @Override
    public void setup() {
        super.setup();
        cptPaint = 0;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(NaimiTrehelGui.this);
                    tableau.setTitle(String.format("Tableau Blanc de %d", getId()));
                }
            }
        });
    }

    /*
    Proc 0 envoie des formes.
    Proc 1 decide d'en envoyer aussi après.
    Proc 0 recoit le REQ de Proc 1 mais n'envoie pas le token.
    Proc 1 pense qu'il est le owner du token aussi.
     */
    @Override
    public void onMessage(SendToMessage message) {
        super.onMessage(message);
        if(message.getData() instanceof Forme) {
            synchronized (tableauLock) {
                paintForme((Forme) message.getData());
            }
            sendToNode(message.getFrom(), new PaintOK());
        }
        else if(message.getData() instanceof PaintOK) {
            cptPaint++;
            if(cptPaint == getNetSize()-1 && sc == true) {
                endCriticalUse();
            }
        }
    }

    public void criticalSection() {
        cptPaint = 0;
        sendToAllNode(forme);
    }

    // Rule 5
    public void endCriticalUse() {
        sc = false;
        if (next > -1) {
            sendToNode(next, new Token());
            token = false;
            next = -1;
        }
    }

    private void paintForme(Forme forme) {
        if (forme == null) {
            return;
        }
        synchronized (tableauLock) {
            if (tableau == null) {
                //tmpPaintQueue.add(forme);
            } else {
                tableau.delivreForme(forme);
            }
        }
    }

    @Override
    public void onPaint(Forme forme) {
        this.forme = forme;
        // Rule 2
        sc = true;
        if(owner > -1) {
            sendToNode(owner, new REQMessage(getId()));
            synchronized (tokenLock) {
                while (token != true) {
                    try {
                        tokenLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        criticalSection();
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

    @Override
    public Object clone() {
        return new NaimiTrehelGui();
    }
}
