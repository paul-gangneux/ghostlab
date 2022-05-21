package launcher;

import ui.*;
import client.*;
import model.GameInfo;
// import model.PlayerModel;
import model.PlayerModel;

public class Launcher {

    private static boolean verbose = false;
    private static boolean veryVerbose = false;
    private static boolean quit = false;
    public static final Object waitObj = new Object();

    private static final String HELP_TEXT = 
        "usage:\n" +
        "       ./client.jar [-options] [parameters]\n" +
        "or:\n" +
        "       java -jar client.jar [-options] [parameters]\n\n" +
        "options:\n" +
        "       -v\n" +
        "           Verbose mode. Prints informations on STDOUT.\n\n" +
        "       -V\n" +
        "           Very verbose mode. Prints all circulating network messages\n"+
        "           on STDOUT.\n\n" +
        "       -a address\n" +
        "           Uses the parameter as address to connect to the server.\n" +
        "           Default adress is localhost.\n\n" +
        "       -p port\n" +
        "           Uses the parameter as TCP port to connect to the server.\n" +
        "           Default port is 4242.\n\n" +
        "       -t delay\n" +
        "           Sets time it takes (in seconds) for ghost images to fade\n" +
        "           out from the game view. default is 4.\n\n" +
        "       -h\n" +
        "           Prints help.";

    public static void main(String[] args) {

        String serverIp = "localhost";
        int tcpPort = 4242;
        int fadeout = 4;

        for (int i = 0; i < args.length; i++) {
            if (args[i].charAt(0) == '-') {
                if (args[i].contains("v")) {
                    setVerbose(true);
                }
                if (args[i].contains("V")) {
                    setVeryVerbose(true);
                }
                if (args[i].contains("a")) {
                    try {
                        serverIp = args[i + 1];
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(HELP_TEXT);
                        System.exit(0);
                    }
                }
                if (args[i].contains("p")) {
                    try {
                        tcpPort = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        System.out.println(HELP_TEXT);
                        System.exit(0);
                    }
                }
                if (args[i].contains("t")) {
                    try {
                        fadeout = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        System.out.println(HELP_TEXT);
                        System.exit(0);
                    }
                }
                if (args[i].contains("h")) {
                    System.out.println(HELP_TEXT);
                    System.exit(0);
                }
            }
        }

        if (fadeout <= 1) {
            View.setFadeout(600);
            View.setFadeoutMem(400);
        } else {
            fadeout *= 1000;
            View.setFadeout(fadeout/2);
            View.setFadeoutMem(fadeout/2);
        }
        
        PlayerModel.initialize("Player1");
        View.initialize();
        Client.initialize(serverIp, tcpPort);
        GameInfo.setCurrentGameInfo(new GameInfo(0, 0, 0, 0));
        Client.getInstance().startInteraction();

        loops();

        // exiting program
    }

    public static void loops() {

        while (!quit) {
            if (verbose)
                System.out.println("loops");
            try {
                if (verbose)
                    System.out.println("waiting");
                synchronized(waitObj) {
                    waitObj.wait();
                }
                if (verbose)
                    System.out.println("notified");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
            if (!quit) {
                Client.getInstance().resetTcpConnection();
            }
        }
    }

    public static void setVerbose(boolean v) {
        verbose = v;
    }

    public static boolean isVerbose() {
        return verbose;
    }

    public static void setVeryVerbose(boolean v) {
        veryVerbose = v;
    }

    public static boolean isVeryVerbose() {
        return veryVerbose;
    }
}