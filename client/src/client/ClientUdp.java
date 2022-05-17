package client;

import java.net.*;

public class ClientUdp extends Thread {
    private int port;
    private InetAddress address;
    private DatagramSocket dso = null;

    public ClientUdp(String ip) {
        try {
            this.address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        int p = 5000;
        boolean b = true;

        while (b) {
            try {
                dso = new DatagramSocket(p);
                b = false;
            } catch (SocketException e) {
                p++;
                if (p > 10000) {
                    System.out.println("Error at ClientUdp builder");
                    System.exit(1);
                }
            }
        }
        this.port = p;
    }

    public int getPort() {
        return port;
    }

    @Override
    public void run() {

        try {

            byte[] data = new byte[100];
            while (true) {
                DatagramPacket paquet = new DatagramPacket(data, data.length, address, port);
                // Prepare the packet for receive
                paquet.setData(new byte[100]);
                // receive a response from the server
                dso.receive(paquet);
                String st = new String(paquet.getData(), 0, paquet.getLength());
                System.out.println("J'ai reçu :" + st);
                System.out.println("De la machine " + paquet.getAddress().toString());
                System.out.println("Depuis le port " + paquet.getPort());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dso != null)
                dso.close();
        }
    }
}