package client;

import model.PlayerModel;

public class Client {
    private ClientTcp c1;
    private ClientUdp c2;

    static Client client = null;  

    private Client(String serverIp, int tcpPort) {
        c1 = new ClientTcp(serverIp, tcpPort);
        //TODO: générer un port UDP différent pour chaque client
        c2 = new ClientUdp(serverIp, 5555);
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

    public void createGame(String username) {
        String StrMsg = "NEWPL "+username+" "+c2.port+"***";
        System.out.println("created "+username);
        byte[] msg = StrMsg.getBytes();
        //TODO: récuperer username et port depuis les infos client
        c1.sendToServer(msg);
    }
}
