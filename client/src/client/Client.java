package client;

public class Client {
    private ClientTcp c1;
    private ClientUdp c2;

    static Client client = null;  

    private Client(String serverIp, int tcpPort) {
        c1 = new ClientTcp(serverIp, tcpPort);
        c2 = new ClientUdp(serverIp);
    }

    public static void initialize(String serverIp, int tcpPort) {
        if (client == null) client = new Client(serverIp, tcpPort);
    }

    public static Client getInstance() {
        return client;
    }
    
    public void startInteraction() {
        c1.start();
        c2.start();
    }

    public void createGame() {
        byte[] msg = ("NEWPL username "+c2.getPort()+"***").getBytes();
        //TODO: récuperer username depuis les infos client
        c1.sendToServer(msg);
    }
}
