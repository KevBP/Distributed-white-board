package lelann;

/**
 * Created by maxime on 02/11/2015.
 */
public class Token<T> {
    private final T data;

    public Token(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
