package client;

import model.PlayerModel;

public class Client {

    private ClientUdp cliUdp = null;
    private String serverIp;
    private int tcpPort;

    static Client client = null;

    private Client(String serverIp, int tcpPort) {
        this.serverIp = serverIp;
        this.tcpPort = tcpPort;
        ClientTcp.setTcpSocket(serverIp, tcpPort);
        cliUdp = new ClientUdp();
    }

    public static void initialize(String serverIp, int tcpPort) {
        if (client == null)
            client = new Client(serverIp, tcpPort);
    }

    public static Client getInstance() {
        return client;
    }

    public void startInteraction() {
        ClientTcp.startListening();
        cliUdp.start();
    }

    public void resetTcpConnection() {
        ClientTcp.setTcpSocket(serverIp, tcpPort);
        ClientTcp.startListening();
    }

    public void joinGame(int gameId, String username) {
        byte[] msg = ("REGIS username " + cliUdp.getPort() + " i***").getBytes();
        int n = username.length();
        for (int i = 0; i < 8; i++) {
            if (i < n) {
                msg[6 + i] = username.getBytes()[i];
            } else {
                msg[6 + i] = 0;
            }
        }
        msg[20] = (byte) gameId;
        ClientTcp.sendToServer(msg);
    }

    public void createGame(String username) {
        byte[] msg = ("NEWPL username " + cliUdp.getPort() + "***").getBytes();
        int n = username.length();
        for (int i = 0; i < 8; i++) {
            if (i < n) {
                msg[6 + i] = username.getBytes()[i];
            } else {
                msg[6 + i] = 0;
            }
        }
        ClientTcp.sendToServer(msg);
    }

    public void ready() {
        ClientTcp.sendToServer("START***");
    }

    public void askSize(int gameId) {
        byte[] msg = ("SIZE? i***").getBytes();
        msg[6] = (byte) gameId;
        ClientTcp.sendToServer(msg);
    }

    public void askPlayerList(int gameId) {
        byte[] msg = ("LIST? i***").getBytes();
        msg[6] = (byte) gameId;
        ClientTcp.sendToServer(msg);
    }

    public void askForGameList() {
        ClientTcp.sendToServer("GAME?***");
    }

    public void sendMessToAll(String mi) {
        ClientTcp.sendToServer("MALL? " + mi + "***");
    }

    public void askPlayers() {
        ClientTcp.sendToServer("GLIS?***");
    }

    public void sendPrivateMess(String mi, String username) {
        byte[] msg = ("SEND? username " + mi + "***").getBytes();
        int n = username.length();
        for (int i = 0; i < 8; i++) {
            if (i < n) {
                msg[6 + i] = username.getBytes()[i];
            } else {
                msg[6 + i] = 0;
            }
        }
        ClientTcp.sendToServer(msg);
    }

    public void move(int amount, int direction) {
        String a = String.valueOf(amount);
        if (a.length() == 1) {
            a = "00" + a;
        } else if (a.length() == 2) {
            a = "0" + a;
        }

        switch (direction) {
            case PlayerModel.MV_DO:
                ClientTcp.sendToServer("DOMOV " + a + "***");
                break;
            case PlayerModel.MV_UP:
                ClientTcp.sendToServer("UPMOV " + a + "***");
                break;
            case PlayerModel.MV_LE:
                ClientTcp.sendToServer("LEMOV " + a + "***");
                break;
            case PlayerModel.MV_RI:
                ClientTcp.sendToServer("RIMOV " + a + "***");
                break;
            default:
                break;
        }
    }

    public void askForLight() {
        ClientTcp.sendToServer("LIGH?***");
    }

    public void quitting() {
        ClientTcp.sendToServer("IQUIT***");
    }

    public void unReg() {
        ClientTcp.sendToServer("UNREG***");
    }

    public void updateGameInfos(int id) {
        askSize(id);
        askPlayerList(id);
    }
}
