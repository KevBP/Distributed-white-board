package lelann;

/**
 * Created by maxime on 02/11/2015.
 */
public class LeLannImpl<T> extends LeLann<T> {
    @Override
    public Token<T> initToken() {
        return new Token<>(null);
    }

    @Override
    public Token criticalSection(Token<T> token) {
        System.out.println("CRITICAL SUB");
        return token;
    }

    @Override
    public Object clone() {
        return new LeLannImpl<T>();
    }
}
