package client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

// import java.util.Scanner;
import model.GameInfo;
import model.PlayerModel;
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

    public String getPosX(byte[] buf){
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 15] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 15, n, StandardCharsets.UTF_8);
        if(pos.length()==1)pos="00"+pos;
        if(pos.length()==2)pos="0"+pos;
        return pos;

    }

    public String getPosY(byte[] buf){
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 19] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 19, n, StandardCharsets.UTF_8);
        if(pos.length()==1)pos="00"+pos;
        if(pos.length()==2)pos="0"+pos;
        return pos;
    }

    public String getPosXOnMove(byte[] buf){
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 6] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 6, n, StandardCharsets.UTF_8);
        if(pos.length()==1)pos="00"+pos;
        if(pos.length()==2)pos="0"+pos;
        return pos;
    }

    public String getPosYOnMove(byte[] buf){
        int n = 0;
        for (int i = 0; i < 3; i++) {
            if (buf[i + 10] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 10, n, StandardCharsets.UTF_8);
        //fill the numbers with 0 to get a number of 3 digits
        if(pos.length()==1)pos="00"+pos;
        if(pos.length()==2)pos="0"+pos;
        return pos;
    }

    public String getPoints(byte[] buf){
        int n = 0;
        for (int i = 0; i < 4; i++) {
            if (buf[i + 14] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 14, n, StandardCharsets.UTF_8);
        if(pos.length()==1)pos="000"+pos;
        if(pos.length()==2)pos="00"+pos;
        if(pos.length()==3)pos="0"+pos;

        return pos;
    }

    public String getPointsOnGlis(byte[] buf){
        int n = 0;
        for (int i = 0; i < 4; i++) {
            if (buf[i + 23] != 0) {
                n++;
            } else {
                break;
            }
        }
        String pos = new String(buf, 23, n, StandardCharsets.UTF_8);
        if(pos.length()==1)pos="000"+pos;
        if(pos.length()==2)pos="00"+pos;
        if(pos.length()==3)pos="0"+pos;

        return pos;
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

                case "UNROK": { // [UNROK m***]
                    // TODO
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

                case "LIST!": { // [[LIST! m s***]
                    int num = buf[8];
                    ArrayList<String> infoPlayers = new ArrayList<>();
                    for (int i = 0; i < num; i++) {
                        try {
                            size = readMessage(istream, buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        keyword = getKeyword(buf);

                        if (size != 12 || !keyword.equals("PLAYR")) {
                            System.out.println("error at game info reading");
                            break;
                        }
                        String playerid = ClientUdp.getPseudo(buf);
                        infoPlayers.add(playerid);
                    }

                    // TODO: read all
                    // [PLAYR username***]
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
                    System.out.println(id+" "+getPosX(buf)+" "+getPosY(buf));
                    PlayerModel pm = new PlayerModel(id,x_pos, y_pos);
                    View.getInstance().posit(pm);
                    break;
                }

                case "MOVE!": { // [MOVE! xxx yyy***]
                    int x_pos = Integer.parseInt(getPosXOnMove(buf));
                    int y_pos = Integer.parseInt(getPosYOnMove(buf));
                    System.out.println(getPosXOnMove(buf)+" "+getPosYOnMove(buf));
                    PlayerModel pm = new PlayerModel(x_pos, y_pos);
                    View.getInstance().move(pm);
                    break;
                }

                case "MOVEF": { // [MOVEF xxx yyy pppp***]
                    int points =Integer.parseInt(getPoints(buf));
                    int x_pos = Integer.parseInt(getPosXOnMove(buf));
                    int y_pos = Integer.parseInt(getPosYOnMove(buf));
                    System.out.println(getPosXOnMove(buf)+" "+getPosYOnMove(buf) +" "+getPoints(buf));
                    PlayerModel pm = new PlayerModel(x_pos, y_pos);
                    pm.setScore(points);
                    View.getInstance().move(pm);
                    break;
                }

                case "GLIS": { // [GLIS! m***]
                    // TODO : read all
                    int s = buf[6];
                    ArrayList<PlayerModel> players  = new ArrayList<>();
                    for (int i = 0; i < s; i++) {
                        try {
                            // expecting [GPLYR username xxx yyy pppp***]
                            size = readMessage(istream, buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        keyword = getKeyword(buf);
                        if (size != 12 || !keyword.equals("GPLYR")) {
                            System.out.println("error at game info reading");
                            break;
                        }
                        String id = ClientUdp.getPseudo(buf);
                        int x_pos = Integer.parseInt(getPosX(buf));
                        int y_pos = Integer.parseInt(getPosY(buf));
                        int points = Integer.parseInt(getPointsOnGlis(buf));
                        players.add(new PlayerModel(id, x_pos,y_pos,points));
                    }
                    View.getInstance().showPlayers(players);
                    break;
                }

                case "MALL!": { // [MALL!***]
                    // TODO
                    break;
                }

                case "SEND!": { // [SEND!***]
                    // TODO
                    break;
                }

                case "NSEND": { // [NSEND***]
                    // TODO
                    break;
                }

                case "GOBYE": { // [GOBYE***]
                    // TODO
                    break;
                }

                case "DUNNO": { // [DUNNO***]
                    // TODO
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