package routing.message;

/**
 * Hello messages are used as a form of greeting.
 * <p>Allow a router to discover other adjacent routers on its local links and networks
 */
public class Hello extends RoutingMessage {
    private final int from;

    public Hello(int from) {
        this.from = from;
    }


    @Override
    public Object getData() {
        return from;
    }

    @Override
    public String toString() {
        return "Hello{" +
                "from=" + from +
                '}';
    }


    @Override
    public Object clone() {
        //can't call super.clone thank to visidia api
        return new Hello(from);
    }
}
