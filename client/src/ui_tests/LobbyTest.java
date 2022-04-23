package ui_tests;

import java.util.ArrayList;

import client.Client;
import model.GameInfo;
import ui.LobbyWindow;

public class LobbyTest {
    public static void main(String [] args)  {
        Client c = new Client(); // Unused but necessary for testing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<GameInfo> testGameInfoList = new ArrayList<GameInfo>();
                for (int i=0; i < 15; i++) {
                    testGameInfoList.add(new GameInfo(i, 12*i + 3, 0, 0)); // total random shit to test, (0, 0) is laby size
                }
                LobbyWindow lw = new LobbyWindow(c);
                lw.getGameListPanel().processGameList(testGameInfoList);
                lw.setVisible(true);
            }
        });
    }
    
}
