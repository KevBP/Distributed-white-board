package routing.message.visitor;


import visidia.simulation.process.messages.Door;

public interface Visitable {
    public void accept(Visitor visitor, Door door);
}
