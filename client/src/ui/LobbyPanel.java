package ui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import java.awt.*;

import ui.panels.lobby.GameListPanel;
import ui.panels.lobby.LobbyButtonPanel;

public class LobbyPanel extends JPanel {

    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 600;

    private GameListPanel glp;
    private LobbyButtonPanel lbp;
    private JScrollPane scrollPane;

    public LobbyPanel() {
        this(DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LobbyPanel(int width, int height) {
        setLayout(new BoxLayout(this,BoxLayout.X_AXIS));
        setLayout(new GridLayout(1,0));
        glp = new GameListPanel(this);
        scrollPane = new JScrollPane(glp);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane);
        lbp = new LobbyButtonPanel(this);
        add(lbp);
        setSize(width, height);
        glp.setScrollPane(scrollPane);
    }

    public void allowGameJoin() {
        lbp.allowJoining();
    }

    public GameListPanel getGameListPanel() {
        return glp;
    }

}
