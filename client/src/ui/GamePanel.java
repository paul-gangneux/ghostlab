package ui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import java.awt.*;

import model.*;
import ui.panels.chat.ChatWholePanel;
import ui.panels.game.LabyDisplayerPanel;

//import java.awt.GridLayout;

public class GamePanel extends JPanel {

    // private Client client;

    public static final int DEFAULT_GAMEWINDOW_WIDTH = 1500;
    public static final int DEFAULT_GAMEWINDOW_HEIGHT = 800;

    // private GridLayout gl;
    private LabyDisplayerPanel ldp;
    private ChatWholePanel cwp;

    private transient GameInfo gameinfo;
    private transient PlayerModel playerModel;

    public GamePanel(GameInfo gameinfo, PlayerModel playermodel) {
        this.gameinfo = gameinfo;
        this.playerModel = playermodel;
        setSize(DEFAULT_GAMEWINDOW_WIDTH, DEFAULT_GAMEWINDOW_HEIGHT);
        // gl = new GridLayout(1, 2, 0, 0);
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        ldp = new LabyDisplayerPanel(this, gameinfo,
                (DEFAULT_GAMEWINDOW_WIDTH * 2) / 3,
                DEFAULT_GAMEWINDOW_HEIGHT);
        // ldp.setAlignmentY(TOP_ALIGNMENT);

        add(ldp, BorderLayout.CENTER);
        cwp = new ChatWholePanel((DEFAULT_GAMEWINDOW_WIDTH) / 3, DEFAULT_GAMEWINDOW_HEIGHT);

        add(cwp, BorderLayout.EAST);

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
