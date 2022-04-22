package src.Client;

public class Client extends Thread {
    static ClientTcp c1;
    static ClientUdp c2;
    public static void main(String[] args) {
        c1 = new ClientTcp("localhost", 4242);
        c2 = new ClientUdp("localhost", 5555);

        c1.run();
        c2.run();
    }
}
