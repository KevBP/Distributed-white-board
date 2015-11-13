package routing.message.visitor;


import routing.message.Hello;
import routing.message.LinkStateUpdate;
import routing.message.SendToMessage;
import visidia.simulation.process.messages.Door;

public interface Visitor {
    void visit(Hello message, Door door);

    void visit(LinkStateUpdate message, Door door);

    void visit(SendToMessage message, Door door);
}
