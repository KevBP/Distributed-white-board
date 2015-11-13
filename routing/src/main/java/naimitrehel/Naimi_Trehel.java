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

    @Override
    public void setup() {
        // Rule 1
        System.out.println("00000000000000000000000000");
        owner = 0;
        next = -1;
        sc = false;
        token = false;
        if (getId() == 0) {
            sendToNode(0, new Token());
            token = true;
            owner = -1;
        }
        System.out.println("11111111111111111111111111");
    }

    @Override
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof REQMessage) { // Rule 3
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
        else if (message.getData() instanceof Token) { // Rule 4
            token = true;
            notify();
        }
    }

    @Override
    public Object clone() {
        return new Naimi_Trehel();
    }
}
