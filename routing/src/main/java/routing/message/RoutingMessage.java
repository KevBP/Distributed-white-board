package routing.message;

import visidia.simulation.process.messages.Message;
import visidia.simulation.process.messages.MessageType;

import java.awt.*;

/**
 * Created by duboi on 22/10/2015.
 */
public abstract class RoutingMessage extends Message {
    public final static MessageType ROUTING_MESSAGE_TYPE = new MessageType("routing", true, Color.BLUE);

    public RoutingMessage(){
        setType(ROUTING_MESSAGE_TYPE);
    }
}
