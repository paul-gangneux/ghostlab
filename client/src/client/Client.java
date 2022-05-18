package client;

import model.PlayerModel;

public class Client {

    private ClientTcp c1;
    private ClientUdp c2;

    static Client client = null;

    private Client(String serverIp, int tcpPort) {
        c1 = new ClientTcp(serverIp, tcpPort);
        c2 = new ClientUdp();
    }

    public static void initialize(String serverIp, int tcpPort) {
        if (client == null)
            client = new Client(serverIp, tcpPort);
    }

    public static Client getInstance() {
        return client;
    }

    public void startInteraction() {
        c1.start();
        c2.start();
    }

    public void joinGame(int gameId, String username) {
        byte[] msg = ("REGIS username " + c2.getPort() + " i***").getBytes();
        int n = username.length();
        for (int i = 0; i < 8; i++) {
            if (i < n) {
                msg[6 + i] = username.getBytes()[i];
            } else {
                msg[6 + i] = 0;
            }
        }
        msg[20] = (byte) gameId;
        System.out.println("created " + username);
        c1.sendToServer(msg);
    }

    public void createGame(String username) {
        byte[] msg = ("NEWPL username " + c2.getPort() + "***").getBytes();
        int n = username.length();
        for (int i = 0; i < 8; i++) {
            if (i < n) {
                msg[6 + i] = username.getBytes()[i];
            } else {
                msg[6 + i] = 0;
            }
        }
        System.out.println("created " + username);
        c1.sendToServer(msg);
    }

    public void ready() {
        c1.sendToServer("START***");
    }

    public void askSize(int gameId) {
        byte[] msg = ("SIZE? i***").getBytes();
        msg[6] = (byte) gameId;
    }

    public void askForGameList() {
        c1.sendToServer("GAME?***");
    }

    public void sendMessToAll(String mi) {
        c1.sendToServer("MALL? "+mi+"***");
    }

    public void move(int amount, int direction) {
        String a = String.valueOf(amount);
        if (a.length() == 1) {
            a = "00" + a;
        }
        else if (a.length() == 2) {
            a = "0" + a;
        }

        switch(direction) {
            case PlayerModel.MV_DO:
            c1.sendToServer("DOMOV "+a+"***");
            break;
            case PlayerModel.MV_UP:
            c1.sendToServer("UPMOV "+a+"***");
            break;
            case PlayerModel.MV_LE:
            c1.sendToServer("LEMOV "+a+"***");
            break;
            case PlayerModel.MV_RI:
            c1.sendToServer("RIMOV "+a+"***");
            break;
            default:
            break;
        }
    }
}
