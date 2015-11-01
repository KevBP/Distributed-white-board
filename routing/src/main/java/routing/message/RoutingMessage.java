package routing.message;

import routing.message.visitor.Visitable;
import routing.message.visitor.Visitor;
import visidia.simulation.process.messages.Door;
import visidia.simulation.process.messages.Message;
import visidia.simulation.process.messages.MessageType;

import java.awt.*;


public abstract class RoutingMessage extends Message implements Visitable{
    public final static MessageType ROUTING_MESSAGE_TYPE = new MessageType("routing", true, Color.BLUE);

    public RoutingMessage(){
        setType(ROUTING_MESSAGE_TYPE);
    }
}
