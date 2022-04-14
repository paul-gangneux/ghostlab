package src.ui;

import javax.swing.JFrame;

import src.model.GameInfo;
import src.model.PlayerModel;
import src.ui.panels.game.LabyDisplayerPanel;

//import java.awt.GridLayout;

public class GameWindow extends JFrame {

    private static final int DEFAULT_GAMEWINDOW_WIDTH = 1000;
    private static final int DEFAULT_GAMEWINDOW_HEIGHT = 1000;

    //private GridLayout gl;
    private LabyDisplayerPanel ldp;

    private GameInfo gameinfo;

    public GameInfo getGameInfo() {
        return gameinfo;
    }

    private PlayerModel playerModel;

    public PlayerModel getPlayerModel() {
        return playerModel;
    }

    public GameWindow(GameInfo gameinfo, PlayerModel playermodel) {
        this.gameinfo = gameinfo;
        playerModel = playermodel;
        // setLayout(gl);
        ldp = new LabyDisplayerPanel(this, gameinfo);
        add(ldp);
        setTitle("Game window");
        setSize(DEFAULT_GAMEWINDOW_WIDTH, DEFAULT_GAMEWINDOW_HEIGHT);
        setLocationRelativeTo(null); // centers the window
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
    }
}
