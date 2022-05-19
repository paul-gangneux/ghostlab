package ui;

import javax.swing.JPanel;

import java.awt.GridLayout;

import ui.panels.lobby.GameListPanel;
import ui.panels.lobby.LobbyButtonPanel;

public class LobbyPanel extends JPanel {

    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 600;

    private GridLayout gl;
    private GameListPanel glp;
    private LobbyButtonPanel lbp;

    public LobbyPanel() {
        this(DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LobbyPanel(int width, int height) {
        gl = new GridLayout(1, 2, 5, 0); // 1 row, 2 columns for the two panels, 5 px horizontal shift, no vertical
                                         // shift
        setLayout(gl);
        glp = new GameListPanel(this);
        add(glp);
        lbp = new LobbyButtonPanel(this);
        add(lbp);
        setSize(width, height);
    }

    public void allowGameJoin() {
        lbp.allowJoining();
    }

    public GameListPanel getGameListPanel() {
        return glp;
    }

}
