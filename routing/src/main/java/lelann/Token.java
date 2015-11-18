package lelann;

/**
 *
 * @param <T> le type de donn�e transport� par le token
 */
public class Token<T> {
    /**
     * Les donn�es que le token transporte.
     */
    private final T data;

    /**
     * Initialise le token avec la valeur pass�e en parametre.
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
