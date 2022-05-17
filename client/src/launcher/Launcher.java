package launcher;

import ui.*;
import client.*;
import model.GameInfo;
// import model.PlayerModel;

public class Launcher {

    private static boolean verbose = false;
    private static boolean veryVerbose = false;

    private static final String HELP_TEXT = 
        "usage:\n" +
        "       ./client.jar [-options] [parameters]\n" +
        "or:\n" +
        "       java -jar client.jar [-options] [parameters]\n\n" +
        "options:\n" +
        "       -v\n" +
        "           Verbose mode. Prints informations on STDOUT\n\n" +
        "       -V\n" +
        "           Very verbose mode. Prints all circulating network messages\n"+
        "           on STDOUT\n\n" +
        "       -a address\n" +
        "           Uses the parameter as adress to connect to the server.\n" +
        "           Default adress is localhost.\n\n" +
        "       -p port\n" +
        "           Uses the parameter as TCP port to connect to the server.\n" +
        "           Default port is 4242.\n\n" +
        "       -h\n" +
        "           Prints help.";

    public static void main(String[] args) {

        String serverIp = "localhost";
        int tcpPort = 4242;

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
                if (args[i].contains("h")) {
                    System.out.println(HELP_TEXT);
                    System.exit(0);
                }
            }
        }

        View.initialize();
        Client.initialize(serverIp, tcpPort);
        GameInfo.setCurrentGameInfo(new GameInfo(0, 0, 0, 0));
        Client.getInstance().startInteraction();
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