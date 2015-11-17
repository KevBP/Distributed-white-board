package lelanngui;

import gui.Forme;
import gui.FormePaintedListener;
import gui.facade.TableauBlanc;
import gui.facade.TableauBlancImpl;
import lelann.LeLann;
import lelann.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;


public class LeLannGui extends LeLann<TokenDataTable> implements FormePaintedListener {

    public static final boolean STRICT_DRAWING_ORDER = true;
    private final TransferQueue<Forme> paintQueue = new LinkedTransferQueue<>();
    private TableauBlanc tableau;

    @Override
    public void setup() {
        super.setup();
        tableau = new TableauBlancImpl(String.format("Tableau Blanc de %d", getId()), this);
    }

    @Override
    public Token<TokenDataTable> initToken() {
        return new Token<>(new TokenDataTable());
    }


    @Override
    public Token criticalSection(Token<TokenDataTable> token) {
        TokenDataTable table = token.getData();
        List<Forme> buff = new ArrayList<>();
        paintQueue.drainTo(buff);
        if (!STRICT_DRAWING_ORDER) {
            tableau.removeFormes(buff);
        }
        boolean painted = false;
        for (Integer node : table) {
            if (!painted && node >= getId()) {
                tableau.paintFormes(buff);
                painted = true;
            }
            if (node != getId()) {
                List<Forme> formes = table.getFormes(node);
                tableau.paintFormes(formes);
            }
        }

        if (!painted) {
            tableau.paintFormes(buff);
        }
        if (buff.size() > 0) {
            table.putFormes(getId(), buff);
        } else {
            table.removeFormes(getId());
        }
        return new Token<>(table);
    }

    @Override
    public Object clone() {
        return new LeLannGui();
    }

    @Override
    public void onPaint(Forme forme) {
        paintQueue.add(forme);
        if (STRICT_DRAWING_ORDER) {
            tableau.removeForme(forme);
        }
    }

    @Override
    public void onExit() {
        tableau.exit();
        super.onExit();
    }
}
