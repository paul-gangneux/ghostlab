package ui_tests;

import java.util.ArrayList;

import model.GameInfo;
import ui.LobbyPanel;

public class LobbyTest {
    public static void main(String [] args)  {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ArrayList<GameInfo> testGameInfoList = new ArrayList<>();
            for (int i=0; i < 15; i++) {
                testGameInfoList.add(new GameInfo(i, 12*i + 3, 0, 0)); // total random shit to test, (0, 0) is laby size
            }
            LobbyPanel lw = new LobbyPanel();
            lw.getGameListPanel().processGameList(testGameInfoList);
            lw.setVisible(true);
        });
    }
    
}
