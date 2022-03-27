package ui_tests;

import java.util.ArrayList;

import model.GameInfo;
import ui.LobbyWindow;

public class LobbyTest {
    public static void main(String [] args)  {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<GameInfo> testGameInfoList = new ArrayList<GameInfo>();
                for (int i=0; i < 15; i++) {
                    testGameInfoList.add(new GameInfo(i, 12*i + 3)); // total random shit to test
                }
                LobbyWindow lw = new LobbyWindow();
                lw.getGameListPanel().processGameList(testGameInfoList);
                lw.setVisible(true);
            }
        });
    }
    
}
