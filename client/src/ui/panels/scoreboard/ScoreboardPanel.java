package ui.panels.scoreboard;

import java.awt.*;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.PlayerModel;

public class ScoreboardPanel extends JPanel {

    public ScoreboardPanel() {
        super();
        setBackground(Color.WHITE);
        // setForeground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    }

    public void initScoreboard() {
        for (PlayerModel pm : PlayerModel.getAllPlayers()) {
            ScoreLabel sl = new ScoreLabel(pm);
            add(sl);
        }
    }

    public void updateScores() {

        for (Component c : getComponents()) {
            remove(c);
        }

        for (PlayerModel pm : PlayerModel.getAllPlayers()) {
            ScoreLabel sl = new ScoreLabel(pm);
            add(sl);
        }

        revalidate();
    }
}
