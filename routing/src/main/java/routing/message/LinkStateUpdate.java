package routing.message;

import routing.table.VisidiaRoutingTable;

/**
 * These messages contain updated information about the state of certain links on the LSDB
 */
public class LinkStateUpdate extends RoutingMessage{

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
}
