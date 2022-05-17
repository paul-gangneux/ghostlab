package client;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

import model.ChatScope;
import model.MessageInfo;
import ui.View;

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

    public String getKeyword(byte[] buf) {
        byte[] keyBytes = new byte[5];
        for (int i = 0; i < 5; i++)
            keyBytes[i] = buf[i];
        return new String(keyBytes, StandardCharsets.UTF_8);
    }

    public String getPseudo(byte[] buf) {
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

        try {

            byte[] data = new byte[200];
            DatagramPacket paquet = new DatagramPacket(data, data.length, address, port);

            while (true) {
                // Prepare the packet for receive
                // paquet.setData(new byte[200]);
                // receive a response from the server
                dso.receive(paquet);
                data = paquet.getData();

                String keyword = getKeyword(data);
                switch (keyword) {
                    case "MESSP":
                        String senderPseudo = "";
                        String message = "";
                        senderPseudo = getPseudo(data);
                        System.out.println("Depuis le pseudo : " + senderPseudo);
                        
                        // starts reading from the message directly until +++
                        String st = new String(paquet.getData(), 15, paquet.getLength());
                        Scanner sc = new Scanner(st);
                        String line = "";
                        try {
                            line = sc.useDelimiter("\\+\\+\\+").next(); // we use delimeter +++ to get only the message
                            System.out.println("message : " + line);
                            MessageInfo messageInfo = new MessageInfo(ChatScope.INCOMING_PRIVATE_MSG, senderPseudo,
                                    message);
                            View.getInstance().incomingMessage(messageInfo);
                            sc.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Error of server reply . Maybe doesn't end with +++");
                        }
                        break;

                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dso != null)
                dso.close();
        }
    }

}