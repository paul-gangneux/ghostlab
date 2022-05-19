package ui.panels.scoreboard;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

import model.PlayerModel;

public class ScoreLabel extends JLabel {

    private PlayerModel pm;

    public ScoreLabel(PlayerModel pm) {
        this.pm = pm;
        setForeground(Color.RED);
        setFont(getFont().deriveFont(Font.BOLD));
        refresh();
    }

    public PlayerModel getPlayerModel() {
        return pm;
    }

    public void refresh() {
        setText(pm.getPseudo() + " : " + Integer.toString(pm.getScore()));
    }
}
