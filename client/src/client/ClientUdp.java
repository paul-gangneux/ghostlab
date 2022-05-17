package client;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import model.ChatScope;
import model.MessageInfo;
import ui.View;

public class ClientUdp extends Thread {

    private int port;
    // private InetAddress address;
    private DatagramSocket dso = null;

    public ClientUdp() {
        // try {
        //     this.address = InetAddress.getByName(ip);
        // } catch (UnknownHostException e) {
        //     e.printStackTrace();
        // }

        int p = 5000; // default port in case
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

    private String getKeyword(byte[] buf) {
        byte[] keyBytes = new byte[5];
        for (int i = 0; i < 5; i++)
            keyBytes[i] = buf[i];
        return new String(keyBytes, StandardCharsets.UTF_8);
    }

    public static String getPseudo(byte[] buf) {
        int n = 0;
        for (int i = 0; i < 8; i++) {
            if (buf[i + 6] != 0) {
                n++;
            } else {
                break;
            }
        }
        return new String(buf, 6, n, StandardCharsets.UTF_8);
    }

    @Override
    public void run() {

        byte[] data = new byte[218];

        DatagramPacket paquet = new DatagramPacket(data, data.length);

        while (true) {

            try {
                dso.receive(paquet);
            } catch (IOException e1) {
                e1.printStackTrace();
                break;
            }

            data = paquet.getData();
            String keyword = getKeyword(data);

            if (keyword.equals("MESSP")) {

                String username = ClientUdp.getPseudo(data);
                System.out.println("Depuis le pseudo : " + username);

                // starts reading from the message directly until +++
                String st = new String(paquet.getData(), 15, paquet.getLength() - 15);
                Scanner sc = new Scanner(st);
                String msg;
                try {
                    msg = sc.useDelimiter("\\+\\+\\+").next(); // we use delimeter +++ to get only the message
                    System.out.println("message : " + msg);
                    MessageInfo messageInfo = new MessageInfo(ChatScope.INCOMING_PRIVATE_MSG, username, msg);
                    View.getInstance().incomingMessage(messageInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Error of server reply . Maybe doesn't end with +++");
                }
                sc.close();
            }

            else {
                System.out.println("clientUdp: bad incoming message (unrecognised keyword)");
            }
        }
        dso.close();

    }

}