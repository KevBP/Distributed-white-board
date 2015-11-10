package naimitrehel;

import naimitrehel.message.REQMessage;
import naimitrehel.message.Token;
import routing.RoutingAlgo;
import routing.message.SendToMessage;

public class Naimi_Trehel extends RoutingAlgo {
    private int owner;
    private boolean sc;
    private boolean token;
    private int next;

    @Override
    public void setup() {
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
        if (message.getData() instanceof REQMessage) { // Rule 2
            if (owner == -1) {
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
        else if (message.getData() instanceof Token) { // Rule 3
            token = true;
        }
    }

    private synchronized void criticalStuff(){
        System.out.println("CRITICAL SUB");
    }

    @Override
    public Object clone() {
        return new Naimi_Trehel();
    }
}
