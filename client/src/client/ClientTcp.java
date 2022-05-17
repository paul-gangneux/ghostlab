package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
// import java.util.Scanner;
import model.GameInfo;
import ui.View;

public class ClientTcp extends Thread {
    private Socket server;
    // private Scanner key; // Scanner for input
    BufferedReader istream;
    PrintWriter ostream;
    String ogameList; // we initialize the list of the games available with a string so we can parse
                      // it later

    public ClientTcp(String ip, int portTcp) {
        try {
            server = new Socket(ip, portTcp);
            // key = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nConnexion refusée. assurez-vous que le serveur a bien été lancé");
            System.exit(1);
        }
        try {
            istream = new BufferedReader(new InputStreamReader(server.getInputStream()));
            ostream = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public boolean checkRequest(String request) {
        if (request.isEmpty() || request.equals("")) {
            System.out.println("ERROR : empty request");
            return false;
        }
        if (!(request.endsWith("***"))) {
            System.out.println("ERROR : bad fomulated request");
            return false;
        }
        return true;
    }

    @Override
    public void run() {
        // boolean end = false;
        byte[] buf = new byte[128];
        // key = new Scanner(System.in);

        while (true) {

            int size = 0;
            try {
                size = readMessage(istream, buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String keyword = getKeyword(buf);

            // pour débugger, à supprimer plus tard
            System.out.println(keyword);

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

                case "SIZE!": { // [SIZE! m hh ww***]
                    int id = 0xff & buf[6];
                    // pour avoir en little-endian:
                    int h = (0xff & buf[8]) + (0xff & buf[9]) * 0x100;
                    int w = (0xff & buf[11]) + (0xff & buf[12]) * 0x100;
                    // TODO: change something else ?
                    GameInfo.setCurrentGameInfo(new GameInfo(id, 0, h, w));
                    break;
                }

                case "WELCO": { // [WELCO m hh ww f ip_.___.___.___ port***]
                    int id = 0xff & buf[6];
                    // pour avoir en little-endian:
                    int h = (0xff & buf[8]) + (0xff & buf[9]) * 0x100;
                    int w = (0xff & buf[11]) + (0xff & buf[12]) * 0x100;
                    // TODO : mettre à jour les autres infos
                    // TODO : se connecter au multicast
                    GameInfo.setCurrentGameInfo(new GameInfo(id, 0, h, w));
                    View.getInstance().showGame();
                    break;
                }

                default:
                    System.out.println("message non compris");
                    break;
            }
        }

    }

    // return true on success, false on failure
    public boolean sendToServer(byte[] data) {
        try {
            server.getOutputStream().write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // return true on success, false on failure
    public boolean sendToServer(String s) {
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
    public int readMessage(BufferedReader in, byte[] buf) throws IOException {
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
    public String getKeyword(byte[] buf) {
        byte[] keyBytes = new byte[5];
        for (int i = 0; i < 5; i++)
            keyBytes[i] = buf[i];
        return new String(keyBytes, StandardCharsets.UTF_8);
    }

    // get the local port
    public int getPort() {
        return this.server.getLocalPort();
    }

    // get the local inet adress
    public InetAddress getAdress() {
        return this.server.getLocalAddress();
    }
}