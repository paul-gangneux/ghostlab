package ui;

import javax.swing.JFrame;
import javax.swing.JPanel;

import model.GameInfo;
import model.PlayerModel;
import ui.panels.game.LabyDisplayerPanel;

//import java.awt.GridLayout;

public class GameWindow extends JPanel {

    private static final int DEFAULT_GAMEWINDOW_WIDTH = 1000;
    private static final int DEFAULT_GAMEWINDOW_HEIGHT = 1000;

    //private GridLayout gl;
    private LabyDisplayerPanel ldp;

    private transient GameInfo gameinfo;

    public GameInfo getGameInfo() {
        return gameinfo;
    }

    private transient PlayerModel playerModel;

    public PlayerModel getPlayerModel() {
        return playerModel;
    }

    public GameWindow(GameInfo gameinfo, PlayerModel playermodel) {
        this.gameinfo = gameinfo;
        playerModel = playermodel;
        // setLayout(gl);
        ldp = new LabyDisplayerPanel(this, gameinfo);
        add(ldp);
        // setTitle("Game window");
        setSize(DEFAULT_GAMEWINDOW_WIDTH, DEFAULT_GAMEWINDOW_HEIGHT);
        // setLocationRelativeTo(null); // centers the window
        // setResizable(false);
        // setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE); 
    }
}
