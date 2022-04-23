package ui;

import javax.swing.JFrame;

import client.Client;

import java.awt.GridLayout;

import ui.panels.lobby.GameListPanel;
import ui.panels.lobby.LobbyButtonPanel;

public class LobbyWindow extends JFrame {

    private Client client;
    
    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 600;

    private GridLayout gl;
    private GameListPanel glp;
    private LobbyButtonPanel lbp;
    
    public LobbyWindow(Client client) {
        this(client, DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LobbyWindow(Client client, int width, int height) {
        this.client = client;
        gl = new GridLayout(1, 2, 5, 0); // 1 row, 2 columns for the two panels, 5 px horizontal shift, no vertical shift
        setLayout(gl);
        glp = new GameListPanel(client, this);
        add(glp);
        lbp = new LobbyButtonPanel(client, this);
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
