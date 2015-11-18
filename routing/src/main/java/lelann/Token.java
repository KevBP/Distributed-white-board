package lelann;

/**
 *
 * @param <T> le type de donnée transporté par le token
 */
public class Token<T> {
    /**
     * Les données que le token transporte.
     */
    private final T data;

    /**
     * Initialise le token avec la valeur passée en parametre.
     *
     * @param data
     */
    public Token(T data) {
        this.data = data;
    }

    /**
     *
     * @return
     */
    public T getData() {
        return data;
    }
}
