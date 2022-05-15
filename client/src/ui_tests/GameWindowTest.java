package ui_tests;

import model.GameInfo;
import model.PlayerModel;
import ui.GamePanel;

public class GameWindowTest {
    public static void main(String [] args)  {
        javax.swing.SwingUtilities.invokeLater( () -> {
            GameInfo gi = new GameInfo(1, 4, 10, 5); // game #1, 4 players, laby is 10x5.
            PlayerModel pm = new PlayerModel(2, 2); // player is set to be at (2, 2)
            GamePanel gw = new GamePanel(gi, pm);
            gw.setVisible(true);
        });
    }
}
