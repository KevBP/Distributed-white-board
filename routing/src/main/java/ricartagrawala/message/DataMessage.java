package ricartagrawala.message;


public class DataMessage<T> {
    private final T data;

    public DataMessage(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
