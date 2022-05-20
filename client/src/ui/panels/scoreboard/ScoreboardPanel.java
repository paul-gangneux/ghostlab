package ui.panels.scoreboard;

import java.awt.*;
import javax.swing.*;

import model.PlayerModel;

import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;

public class ScoreboardPanel extends JPanel {

    HashMap<String, ScoreLabel> labelList;

    private class ScoreLabel extends JLabel {

        public ScoreLabel(PlayerModel pm) {
            setForeground(Color.PINK);
            setFont(getFont().deriveFont(14));
            setFont(getFont().deriveFont(Font.BOLD));
            updateText(pm);
        }

        public void updateText(PlayerModel pm) {
            EventQueue.invokeLater(() -> {
                setText(pm.getPseudo() + " :       " + Integer.toString(pm.getScore()) + "     ");
                revalidate();
            });
        }
    }

    public ScoreboardPanel() {
        super();
        labelList = null;
        setBackground(Color.BLACK);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 4));
    }

    public void initScoreboard() {
        labelList = new HashMap<>();
        for (PlayerModel pm : PlayerModel.getAllPlayers()) {
            ScoreLabel sl = new ScoreLabel(pm);
            labelList.put(pm.getPseudo(), sl);
            add(sl);
        }
    }

    public void updateScores() {
        if (labelList == null) {
            initScoreboard();
        } else {
            for (PlayerModel pm : PlayerModel.getAllPlayers()) {
                ScoreLabel sl = labelList.get(pm.getPseudo());
                if (sl != null) {
                    sl.updateText(pm);
                }
            }
        }
    }
}
