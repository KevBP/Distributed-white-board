package lelann;

import routing.RoutingAlgo;
import routing.message.SendToMessage;

/**
 * Algorithme de LeLann sur un reseau quelconque.
 * <br/> Utilise le systeme de route implémenté dans {@link RoutingAlgo}
 *
 * @param <T> le type de donnée contenue dans le token.
 * @see RoutingAlgo
 */
public abstract class LeLann<T> extends RoutingAlgo {
    /**
     * Un objet pour faire de la synchronisation lors de la section critique.
     */
    protected final Object criticalSectionLock = new Object();

    @Override
    public void setup() {
        int firstNode = firstNode();
        // si le noeud est le 1er du reseau
        if (getId() == firstNode) {
            // initie le token ring en simulant une reception du token
            sendToNode(firstNode, initToken());
        }
    }

    /**
     * Methode pour definir quel est le 1er noeud du reseau.
     *
     * @return le 1er noeud du reseau
     */
    public int firstNode() {
        return 0;
    }

    /**
     * Methode pour créer et initialiser le 1er {@link Token}.
     * @return un {@link Token} a utiliser
     */
    public abstract Token<T> initToken();

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(SendToMessage message) {
        if (message.getData() instanceof Token) {
            Token token;
            // acces a la section critique
            synchronized (criticalSectionLock) {
                token = criticalSection((Token<T>) message.getData());
            }
            // envoi du token au prochain noeud du reseau
            sendToNode(nextNode(), token);
        }
        throw new IllegalArgumentException("Unexpected message " + message);
    }

    /**
     * La section critique.
     * <br/> Cette methode doit retourner un {@link Token} qui sera envoyer au prochain noeud du reseau.
     * @param token le {@link Token} reçut
     * @return le {@link Token} a envoyer au prochain noeud du reseau
     */
    public abstract Token criticalSection(Token<T> token);

    /**
     * Methode pour definir quel est le prochain noeud du reseau.
     * @return prochain noeud du reseau
     */
    public int nextNode() {
        return (getId() + 1) % getNetSize();
    }
}
