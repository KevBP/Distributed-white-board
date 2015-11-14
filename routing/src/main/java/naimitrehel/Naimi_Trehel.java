package naimitrehel;

import naimitrehel.message.REQMessage;
import naimitrehel.message.Token;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

public class Naimi_Trehel extends RoutingAlgo {
    protected int owner;
    protected boolean sc;
    protected boolean token;
    protected int next;
    protected final Object tokenLock = new Object();

    @Override
    public void setup() {
        // Rule 1
        owner = 0;
        next = -1;
        sc = false;
        token = false;
        if (getId() == 0) {
            sendToNode(0, new Token());
            token = true;
            owner = -1;
        }
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 3
            System.out.println(owner);
            if (owner == -1) {
                System.out.println(sc);
                if (sc == true) {
                    next = message.getFrom();
                }
                else {
                    token = false;
                    sendToNode(((REQMessage) message.getData()).getFrom(), new Token());
                }
            }
            else {
                sendToNode(owner, new REQMessage(((REQMessage) message.getData()).getFrom()));
            }
            owner = ((REQMessage) message.getData()).getFrom();
        }
        else if (message.getData() instanceof Token) { // Rule 4
            synchronized (tokenLock) {
                token = true;
                owner = -1;
                notify();
            }
        }
    }

    @Override
    public Object clone() {
        return new Naimi_Trehel();
    }
}
