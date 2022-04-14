package src.ui_tests;

import java.util.ArrayList;

import src.model.GameInfo;
import src.ui.LobbyWindow;

public class LobbyTest {
    public static void main(String [] args)  {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ArrayList<GameInfo> testGameInfoList = new ArrayList<GameInfo>();
                for (int i=0; i < 15; i++) {
                    testGameInfoList.add(new GameInfo(i, 12*i + 3, 0, 0)); // total random shit to test, (0, 0) is laby size
                }
                LobbyWindow lw = new LobbyWindow();
                lw.getGameListPanel().processGameList(testGameInfoList);
                lw.setVisible(true);
            }
        });
    }
    
}
