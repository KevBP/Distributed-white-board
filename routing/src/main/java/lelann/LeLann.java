package lelann;

import routing.RoutingAlgo;
import routing.message.SendToMessage;

public abstract class LeLann<T> extends RoutingAlgo {
    protected final Object criticalSectionLock = new Object();

    @Override
    public void setup() {
        if (getId() == 0){
            sendToNode(0, initToken());
        }
    }

    public abstract Token<T> initToken();

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof Token){
            Token token;
            synchronized (criticalSectionLock) {
                token = criticalSection((Token<T>) message.getData());
            }
            sendToNode(nextNode(), token);
        }
    }


    public abstract Token criticalSection(Token<T> token);

    public int nextNode(){
        return (getId() + 1) % getNetSize();
    }
}
