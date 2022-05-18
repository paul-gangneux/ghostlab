package ui.panels.scoreboard;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

import model.PlayerModel;

public class ScoreLabel extends JLabel {

    private PlayerModel pm;

    public ScoreLabel(PlayerModel pm) {
        this.pm = pm;
        refresh();
    }

    public void refresh() {
        setForeground(Color.RED);
        setFont(getFont().deriveFont(Font.BOLD));
        setText(pm.getPseudo() + " : " + Integer.toString(pm.getScore()));
    }
}
