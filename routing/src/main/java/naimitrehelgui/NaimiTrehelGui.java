package naimitrehelgui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.TableauBlancUI;
import naimitrehel.Naimi_Trehel;
import naimitrehel.message.REQMessage;
import naimitrehel.message.Token;
import routing.message.SendToMessage;

import javax.swing.*;

public class NaimiTrehelGui extends Naimi_Trehel implements FormePaintedListener {
    private final Object tableauLock = new Object();
    private TableauBlancUI tableau;
    private Forme forme;

    @Override
    public void setup() {
        super.setup();
        System.out.println("22222222222222222222222222");
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (tableauLock) {
                    tableau = new TableauBlancUI(NaimiTrehelGui.this);
                }
            }
        });
    }

    @Override
    public void onMessage(SendToMessage message) {
        super.onMessage(message);
        // TODO
        if(message.getData() instanceof Forme) {
            synchronized (tableauLock) {
                tableau.delivreForme(forme);
            }
        }
    }

    public void criticalSection() {
        // TODO Critical Section
        sendToAllNode(forme);

        // Rule 5: endCriticalUse
        sc = false;
        if (next > -1) {
            sendToNode(next, new Token());
            token = false;
            next = -1;
        }
    }

    @Override
    public void onPaint(Forme forme) {
        this.forme = forme;
        // Rule 2
        sc = true;
        if(owner > -1) {
            sendToNode(owner, new REQMessage(getId()));
            owner = -1;
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        criticalSection();
    }
}
