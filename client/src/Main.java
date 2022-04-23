

import client.Client;
import ui.LauncherWindow;
import ui.View;

public class Main {
    public static void main(String [] args)  {
        Client c = new Client(); // Unused but necessary for testing
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new View(c).switchToWindow(new LauncherWindow(c));
            }
        });
    }
}