package ui;

import javax.swing.JPanel;

import java.awt.GridLayout;

import client.Client;
import model.*;
import ui.panels.chat.ChatWholePanel;
import ui.panels.game.LabyDisplayerPanel;

//import java.awt.GridLayout;

public class GamePanel extends JPanel {

    private Client client;

    private static final int DEFAULT_GAMEWINDOW_WIDTH = 1500;
    private static final int DEFAULT_GAMEWINDOW_HEIGHT = 800;

    private GridLayout gl;
    private LabyDisplayerPanel ldp;
    private ChatWholePanel cwp;

    private transient GameInfo gameinfo;
    private transient PlayerModel playerModel;

    public GamePanel(GameInfo gameinfo, PlayerModel playermodel) {
        this.gameinfo = gameinfo;
        this.playerModel = playermodel;
        gl = new GridLayout(1, 2, 0, 0);
        setLayout(gl);
        ldp = new LabyDisplayerPanel(this, gameinfo);
        add(ldp);
        cwp = new ChatWholePanel(this);
        add(cwp);
        setSize(DEFAULT_GAMEWINDOW_WIDTH, DEFAULT_GAMEWINDOW_HEIGHT);
        // setLocationRelativeTo(null); // centers the window
        // setResizable(false);
        // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE); 
    }

    public GameInfo getGameInfo() {
        return gameinfo;
    }

    public PlayerModel getPlayerModel() {
        return playerModel;
    }

    public LabyDisplayerPanel getLabyDisplayerPanel() {
        return ldp;
    }

    public ChatWholePanel getChatWholePanel() {
        return cwp;
    }

    public void addMessage(MessageInfo mi) {
        cwp.addMessage(mi);
    }

 
}
