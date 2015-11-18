package gui.facade;

import gui.Forme;
import gui.FormePaintedListener;


public interface TableauBlanc {
    void paintForme(Forme forme);

    void paintFormes(Iterable<Forme> formes);

    void removeForme(Forme forme);

    void removeFormes(Iterable<Forme> formes);

    void exit();

    void setFormePaintedListener(FormePaintedListener listener);

    void setTitle(String title);
}
