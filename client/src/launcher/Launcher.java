package launcher;

import ui.*;
import client.*;
import model.GameInfo;

public class Launcher {
    public static void main(String [] args) {
        // TODO : parse arguments properly to get ip and port
        String serverIp = "localhost";
        int tcpPort = 4242;
        if (args.length >= 2) {
            serverIp = args[0];
            tcpPort = Integer.parseInt(args[1]);
        }
        else if (args.length == 1) {
            tcpPort = Integer.parseInt(args[0]);
        }
        View.initialize();
        Client.initialize(serverIp, tcpPort);
        GameInfo.setCurrentGameInfo(new GameInfo(0, 0, 0, 0));
        Client.getInstance().startInteraction(); 
    }
}