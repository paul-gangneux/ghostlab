package src.ui;

import javax.swing.JFrame;

import java.awt.GridLayout;

import src.ui.panels.lobby.GameListPanel;
import src.ui.panels.lobby.LobbyButtonPanel;

public class LobbyWindow extends JFrame {
    
    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 600;

    private GridLayout gl;
    private GameListPanel glp;
    private LobbyButtonPanel lbp;
    
    public LobbyWindow() {
        this(DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LobbyWindow(int width, int height) {
        gl = new GridLayout(1, 2, 5, 0); // 1 row, 2 columns for the two panels, 5 px horizontal shift, no vertical shift
        setLayout(gl);
        glp = new GameListPanel(this);
        add(glp);
        lbp = new LobbyButtonPanel(this);
        add(lbp);
        setTitle("Lobby window");
        setSize(width, height);
        setLocationRelativeTo(null); // centers the window
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    }

    public void allowGameJoin() {
        lbp.allowJoining();
    }

    public GameListPanel getGameListPanel() {
        return glp;
    }

}
