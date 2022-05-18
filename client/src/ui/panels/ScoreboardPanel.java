package ui.panels;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.PlayerModel;

public class ScoreboardPanel extends JPanel {

    private ArrayList<ScoreLabel> scores;

    public ScoreboardPanel() {
        super();
        setBackground(Color.WHITE);
        // setForeground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        scores = new ArrayList<>();
    }

    public void initScoreboard() {
        // TODO get player lists
        PlayerModel[] placeholder = {new PlayerModel("alice", 0, 0), new PlayerModel("bob", 0, 0)};
        for (PlayerModel pm : placeholder) {
            add(new ScoreLabel(pm));
        }
    }

    public void updateScore(PlayerModel pm) {

        for (ScoreLabel sl : scores) {
            sl.refresh();
        }
        revalidate(); // recomputes the layout, effectively refreshing the chat
    }
}
