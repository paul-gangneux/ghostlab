package ui.panels.scoreboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;

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

    public void updateScores() {

        scores.clear();

        for (Component c : getComponents()) {
            remove(c);
        }

        for (PlayerModel pm : PlayerModel.getOtherPlayers()) {
            ScoreLabel sl = new ScoreLabel(pm);
            scores.add(sl);
            add(sl);
        }

        scores.sort(new Comparator<ScoreLabel>() {

            @Override
            public int compare(ScoreLabel arg0, ScoreLabel arg1) {
                return arg0.getPlayerModel().getScore() - arg1.getPlayerModel().getScore();
            }
            
        });

        for (ScoreLabel sl : scores) {
            sl.refresh();
            add(sl);
        }

        revalidate();
    }
}
