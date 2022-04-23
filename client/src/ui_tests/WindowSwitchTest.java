package ui_tests;

import client.Client;
import model.GameInfo;
import model.PlayerModel;
import ui.GameWindow;
import ui.LauncherWindow;
import ui.LobbyWindow;
import ui.View;

public class WindowSwitchTest {
    public static void main(String [] args)  {
        Client c = new Client(); // Unused but necessary for testing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                View v = new View(c);
                GameInfo gi = new GameInfo(1, 4, 10, 5); // game #1, 4 players, laby is 10x5.
                PlayerModel pm = new PlayerModel(2, 2); // player is set to be at (2, 2)
                while (true) {
                    // 2 Fast 4 U
                    v.switchToWindow(new LauncherWindow(c));
                    v.switchToWindow(new LobbyWindow(c));
                    v.switchToWindow(new GameWindow(c, gi, pm));
                }
            }
        });
    }
    // Note : this test works. My eyes paid the price, it's 3 AM and I'm having an epilepsy warning.
    // It's not necessary to inflict that to your eyesight. Trust me, the window swap speed is more than enough.
    // The thing is, it's so fast it cannot load the whole window before the swap, so I cannot estimate the window loading time.
    // Still seems decent, and I guess it's not our main focus. Thread.sleep() is NOT the solution because the invokeLater call 
    // makes it actually pausing the GUI Thread and freezing the app for the duration of the sleep. Yikes.
}
