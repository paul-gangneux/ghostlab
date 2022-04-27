package ui_tests;

import java.util.Random;

import client.Client;
import model.ChatScope;
import model.GameInfo;
import model.MessageInfo;
import model.PlayerModel;
import ui.GameWindow;
import ui.panels.game.LabyTile.TileType;

public class GameWindowTest {
    public static void main(String [] args)  {
        Client c = new Client(); // Unused but necessary for testing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GameInfo gi = new GameInfo(1, 4, 20, 20); // game #1, 4 players, laby is 10x5.
                PlayerModel pm = new PlayerModel(2, 2); // player is set to be at (2, 2)
                GameWindow gw = new GameWindow(c, gi, pm);
                c.setGameWindow(gw);
                gw.setVisible(true);
                gw.addMessage(new MessageInfo(ChatScope.SERVER_MSG, null, "---- HEAD OF CHAT ----"));
                Random rd = new Random();
                TileType[] types = TileType.values();
                for (int i = 0; i < 20; i++) {
                    for (int j = 0; j < 20; j++) {
                        int k = rd.nextInt(9);
                        TileType type = types[k];
                        gw.getLabyDisplayerPanel().getGrid()[i][j].setTile(type, false);
                    }
                }
            }
        });
    }
}
