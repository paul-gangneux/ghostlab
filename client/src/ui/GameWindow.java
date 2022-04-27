package ui;

import javax.swing.JFrame;

import java.awt.GridLayout;

import client.Client;
import model.GameInfo;
import model.MessageInfo;
import model.PlayerModel;
import ui.panels.chat.ChatWholePanel;
import ui.panels.game.LabyDisplayerPanel;

//import java.awt.GridLayout;

public class GameWindow extends JFrame {

    private Client client;

    private static final int DEFAULT_GAMEWINDOW_WIDTH = 1680;
    private static final int DEFAULT_GAMEWINDOW_HEIGHT = 1050;

    private GridLayout gl;
    private LabyDisplayerPanel ldp;
    private ChatWholePanel cwp;

    private GameInfo gameinfo;

    public GameInfo getGameInfo() {
        return gameinfo;
    }

    private PlayerModel playerModel;

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

    public GameWindow(Client client, GameInfo gameinfo, PlayerModel playermodel) {
        this.client = client;
        this.gameinfo = gameinfo;
        this.playerModel = playermodel;
        gl = new GridLayout(1, 2, 0, 0);
        setLayout(gl);
        ldp = new LabyDisplayerPanel(client, this, gameinfo);
        add(ldp);
        cwp = new ChatWholePanel(client, this);
        add(cwp);
        setTitle("Game window");
        setSize(DEFAULT_GAMEWINDOW_WIDTH, DEFAULT_GAMEWINDOW_HEIGHT);
        setLocationRelativeTo(null); // centers the window
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    }
}
