package launcher;

import ui.*;
import client.*;

public class Launcher {
    public static void main(String [] args) {
        String serverIp = "localhost";
        int tcpPort = 4242;
        if (args.length >= 2) {
            serverIp = args[0];
            tcpPort = Integer.parseInt(args[1]);
        }
        View.initialize();
        Client.initialize(serverIp, tcpPort);
        Client.getInstance().startInteraction();

        /*
        javax.swing.SwingUtilities.invokeLater( () -> {
                new LauncherWindow().setVisible(true);
        });
        */
    }
}