package ui.panels.scoreboard;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;

import model.PlayerModel;

public class ScoreLabel extends JLabel {

    public ScoreLabel(PlayerModel pm) {
        setForeground(Color.RED);
        setFont(getFont().deriveFont(Font.BOLD));
        setText(pm.getPseudo() + " :       " + Integer.toString(pm.getScore())+"     ");
    }
}
