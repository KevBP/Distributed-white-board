package routing.message;

import routing.message.visitor.Visitor;
import routing.table.VisidiaRoutingTable;
import visidia.simulation.process.messages.Door;

/**
 * These messages contain updated information about the state of certain links on the LSDB
 */
public class LinkStateUpdate extends RoutingMessage {

    private final VisidiaRoutingTable routingTable;

    public LinkStateUpdate(VisidiaRoutingTable routingTable) {
        this.routingTable = routingTable;
    }

    @Override
    public Object getData() {
        return routingTable;
    }

    @Override
    public String toString() {
        return "LinkStateUpdate";
    }

    @Override
    public Object clone() {
        return new LinkStateUpdate(routingTable);
    }

    @Override
    public void accept(Visitor visitor, Door door) {
        visitor.visit(this, door);
    }
}
