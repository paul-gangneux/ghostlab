package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import launcher.Launcher;
// import java.util.Scanner;
import model.GameInfo;
import model.PlayerModel;
import ui.View;

public class ClientTcp {
    private static Socket server = null;
    private static BufferedReader istream = null;
    private static Thread thread = null;
    private static final Object lock = new Object();

    private ClientTcp() {
    }

    public static void setTcpSocket(String ip, int port) {
        stopListening();
        try {
            server = new Socket(ip, port);
        } catch (IOException e) {
            if (Launcher.isVerbose())
                e.printStackTrace();
            System.out.println("\nConnexion refusée. assurez-vous que le serveur a bien été lancé");
            System.exit(1);
        }
        try {
            istream = new BufferedReader(new InputStreamReader(server.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void startListening() {
        if (thread != null) {
            System.out.println("TCP: Already listening, aborting.");
            return;
        }
        if (server == null) {
            System.out.println("TCP: Attributes not initialized, aborting.");
            return;
        }
        thread = new Thread(ClientTcp::listens);
        thread.start();
    }

    public static void stopListening() {
        synchronized (lock) {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    if (Launcher.isVerbose()) {
                        System.out.println("TCP: Socket already closed");
                    }
                }
            }
            server = null;
        }
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                thread.interrupt();
                e.printStackTrace();
            }
            thread = null;
        }
        if (istream != null) {
            try {
                istream.close();
            } catch (IOException e) {
                e.printStackTrace();
                if (Launcher.isVerbose()) {
                    System.out.println("TCP: istream already closed");
                }
            }
            istream = null;
        }
    }

    // private static boolean checkRequest(String request) {
    // if (request.isEmpty() || request.equals("")) {
    // System.out.println("ERROR : empty request");
    // return false;
    // }
    // if (!(request.endsWith("***"))) {
    // System.out.println("ERROR : bad fomulated request");
    // return false;
    // }
    // return true;
    // }

    private static String getPosX(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 15] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 15, n, StandardCharsets.UTF_8);
        if (pos.length() == 1)
            pos = "00" + pos;
        if (pos.length() == 2)
            pos = "0" + pos;
        return pos;

    }

    private static String getPosY(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 19] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 19, n, StandardCharsets.UTF_8);
        if (pos.length() == 1)
            pos = "00" + pos;
        if (pos.length() == 2)
            pos = "0" + pos;
        return pos;
    }

    private static String getPosXOnMove(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 6] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 6, n, StandardCharsets.UTF_8);
        if (pos.length() == 1)
            pos = "00" + pos;
        if (pos.length() == 2)
            pos = "0" + pos;
        return pos;
    }

    private static String getPosYOnMove(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 10] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 10, n, StandardCharsets.UTF_8);
        // fill the numbers with 0 to get a number of 3 digits
        if (pos.length() == 1)
            pos = "00" + pos;
        if (pos.length() == 2)
            pos = "0" + pos;
        return pos;
    }

    private static String getPoints(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            if (buf[i + 14] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 14, n, StandardCharsets.UTF_8);
        if (pos.length() == 1)
            pos = "000" + pos;
        if (pos.length() == 2)
            pos = "00" + pos;
        if (pos.length() == 3)
            pos = "0" + pos;

        return pos;
    }

    private static String getPointsOnGlis(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 4; i++) {
            if (buf[i + 23] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 23, n, StandardCharsets.UTF_8);
        if (pos.length() == 1)
            pos = "000" + pos;
        if (pos.length() == 2)
            pos = "00" + pos;
        if (pos.length() == 3)
            pos = "0" + pos;

        return pos;
    }

    private static void listens() {
        if (Launcher.isVerbose())
            System.out.println("TCP: starting communication");
        // boolean end = false;
        byte[] buf = new byte[128];
        // key = new Scanner(System.in);
        boolean loops = true;
        while (loops) {

            int size = 0;
            try {
                size = readMessage(istream, buf);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            if (size == 0) {
                System.out.println("TCP: disconnected by server");
                break;
            }

            String keyword = getKeyword(buf);

            if (Launcher.isVeryVerbose()) {
                System.out.println(new String(buf, 0, size, StandardCharsets.UTF_8));
            }

            switch (keyword) {
                case "GAMES": // [GAMES n***]
                    if (size != 10) {
                        System.out.println("wrong size, discarding");
                        break;
                    }
                    int n = buf[6];
                    ArrayList<GameInfo> info = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        try {
                            // expecting [OGAME m s***]
                            size = readMessage(istream, buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        keyword = getKeyword(buf);

                        if (size != 12 || !keyword.equals("OGAME")) {
                            System.out.println("error at game info reading");
                            break;
                        }
                        int gameid = buf[6];
                        int plyrcount = buf[8];
                        info.add(new GameInfo(gameid, plyrcount));
                    }
                    View.getInstance().updateLobbyWindow(info);
                    break;

                case "REGOK": { // [REGOK m***]
                    int id = 0xff & buf[6];
                    GameInfo.getCurrentGameInfo().setId(id);
                    View.getInstance().regOk();
                    break;
                }

                case "REGNO": { // [REGNO***]
                    View.getInstance().regError();
                    break;
                }

                case "UNROK": { // [UNROK m***]
                    View.getInstance().backToLobby();
                    break;
                }

                case "SIZE!": { // [SIZE! m hh ww***]
                    int id = 0xff & buf[6];
                    // pour avoir en little-endian:
                    int h = (0xff & buf[8]) + (0xff & buf[9]) * 0x100;
                    int w = (0xff & buf[11]) + (0xff & buf[12]) * 0x100;
                    View.showGameInfosForSelectedGame(id, h, w);
                    break;
                }

                case "LIST!": { // [[LIST! m s***]
                    int num = buf[8];
                    List<String> userList = new ArrayList<>();
                    for (int i = 0; i < num; i++) {
                        try {
                            size = readMessage(istream, buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        keyword = getKeyword(buf);

                        if (size != 12 || !keyword.equals("PLAYR")) { // [PLAYR username***]
                            System.out.println("error at game info reading");
                            break;
                        }
                        userList.add(ClientUdp.getPseudo(buf));
                    }
                    View.getInstance().showUserListForSectedGame(userList);
                    break;
                }

                case "WELCO": { // [WELCO m hh ww f ip_.___.___.___ port***]
                    int id = 0xff & buf[6];
                    // pour avoir en little-endian:
                    int h = (0xff & buf[8]) + (0xff & buf[9]) * 0x100;
                    int w = (0xff & buf[11]) + (0xff & buf[12]) * 0x100;
                    int f = 0xff & buf[14];

                    String str = new String(buf, 16, 20, StandardCharsets.UTF_8);
                    String ip = str.substring(0, 15);
                    String strPort = str.substring(16, 20);
                    ip = ip.replace("#", "");
                    int port = Integer.parseInt(strPort);

                    ClientMulticast.setMulticastSocket(ip, port);
                    ClientMulticast.startListening();
                    GameInfo.setCurrentGameInfo(new GameInfo(id, 0, h, w));
                    GameInfo.getCurrentGameInfo().setNbGhosts(f);
                    View.getInstance().showGame();
                    break;
                }

                case "POSIT": { // [POSIT username xxx yyy***]
                    String id = ClientUdp.getPseudo(buf);
                    int x_pos = Integer.parseInt(getPosX(buf));
                    int y_pos = Integer.parseInt(getPosY(buf));
                    // System.out.println(id+" "+getPosX(buf)+" "+getPosY(buf));
                    PlayerModel pm = new PlayerModel(id, x_pos, y_pos);
                    View.getInstance().posit(pm);
                    Client.getInstance().askPlayers();
                    Client.getInstance().askForLight();
                    break;
                }

                case "MOVE!": { // [MOVE! xxx yyy***]
                    int x_pos = Integer.parseInt(getPosXOnMove(buf));
                    int y_pos = Integer.parseInt(getPosYOnMove(buf));
                    // System.out.println(getPosXOnMove(buf)+" "+getPosYOnMove(buf));
                    PlayerModel pm = new PlayerModel(x_pos, y_pos);
                    View.getInstance().move(pm);
                    break;
                }

                case "MOVEF": { // [MOVEF xxx yyy pppp***]
                    int points = Integer.parseInt(getPoints(buf));
                    int x_pos = Integer.parseInt(getPosXOnMove(buf));
                    int y_pos = Integer.parseInt(getPosYOnMove(buf));
                    // System.out.println(getPosXOnMove(buf)+" "+getPosYOnMove(buf) +"
                    // "+getPoints(buf));
                    PlayerModel pm = new PlayerModel(x_pos, y_pos);
                    pm.setScore(points);
                    View.getInstance().move(pm);
                    break;
                }

                case "GLIS!": { // [GLIS! m***]
                    int s = buf[6];
                    PlayerModel.getAllPlayers().clear();
                    for (int i = 0; i < s; i++) {
                        try {
                            // expecting [GPLYR username xxx yyy pppp***]
                            size = readMessage(istream, buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        keyword = getKeyword(buf);
                        if (!keyword.equals("GPLYR")) { // size check
                            System.out.println("error at game info reading");
                            break;
                        }
                        String id = ClientUdp.getPseudo(buf);
                        int x_pos = Integer.parseInt(getPosX(buf));
                        int y_pos = Integer.parseInt(getPosY(buf));
                        int points = Integer.parseInt(getPointsOnGlis(buf));
                        PlayerModel.getAllPlayers().add(new PlayerModel(id, x_pos, y_pos, points));
                    }
                    View.getInstance().updatePlayerLists();
                    View.getInstance().showPlayers();
                    break;
                }

                case "LIGHT": { // [LIGHT 12345678***]
                    String lightValues = new String(buf,6, 8, StandardCharsets.UTF_8);
                    View.getInstance().lightSurroundings(lightValues);
                    break;
                }

                case "MALL!": { // [MALL!***]
                    break;
                }

                case "SEND!": { // [SEND!***]
                    View.getInstance().privateMessageSuccess();
                    break;
                }

                case "NSEND": { // [NSEND***]
                    View.getInstance().privateMessageFailure();
                    break;
                }

                case "GOBYE": { // [GOBYE***]
                    loops = false;
                    break;
                }

                case "DUNNO": { // [DUNNO***]
                    break;
                }

                default:
                    System.out.println("message non compris");
                    break;
            }

        }
        try {
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (Launcher.isVerbose())
            System.out.println("TCP: stopping communication");
        ClientMulticast.stopListening();
        View.getInstance().endGame();
    }

    // return true on success, false on failure
    public static boolean sendToServer(byte[] data) {
        try {
            server.getOutputStream().write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // return true on success, false on failure
    public static boolean sendToServer(String s) {
        try {
            server.getOutputStream().write(s.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // reads a single message ending with *** and stores it in buf.
    // returns the number of bytes read
    private static int readMessage(BufferedReader in, byte[] buf) throws IOException {
        int star = 0;
        int n = 0;
        int i = 0;

        while (i < buf.length) {
            n = in.read();
            if (n == -1)
                break;
            buf[i] = (byte) n;
            i++;
            if (n == '*')
                star++;
            else
                star = 0;
            if (star == 3) {
                // morceau de code qui permet d'éviter le problème du byte = 42 avant ***
                if (in.ready()) {
                    // equivalent à un peek
                    in.mark(1);
                    n = in.read();
                    in.reset();
                    if (n == '*') {
                        n = in.read();
                        buf[i] = (byte) n;
                        i++;
                        // on peut lire jusqu'à 2 fois 42 avant un ***
                        in.mark(1);
                        n = in.read();
                        in.reset();
                        if (n == '*') {
                            n = in.read();
                            buf[i] = (byte) n;
                            i++;
                        }
                    }
                }
                break;
            }
        }
        return i;
    }

    // return the first five characters read from the inputstream in string format
    private static String getKeyword(byte[] buf) {
        byte[] keyBytes = new byte[5];
        for (int i = 0; i < 5; i++)
            keyBytes[i] = buf[i];
        return new String(keyBytes, StandardCharsets.UTF_8);
    }

    // // get the local port
    // private int getPort() {
    // return server.getLocalPort();
    // }

    // // get the local inet adress
    // private InetAddress getAdress() {
    // return server.getLocalAddress();
    // }
}