package ui_tests;

import client.Client;
import model.GameInfo;
import model.PlayerModel;
import ui.GameWindow;

public class GameWindowTest {
    public static void main(String [] args)  {
        Client c = new Client(); // Unused but necessary for testing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GameInfo gi = new GameInfo(1, 4, 10, 5); // game #1, 4 players, laby is 10x5.
                PlayerModel pm = new PlayerModel(2, 2); // player is set to be at (2, 2)
                GameWindow gw = new GameWindow(c, gi, pm);
                gw.setVisible(true);
            }
        });
    }
}
