package client;
// import java.io.*;
import java.net.*;
public class ClientUdp extends Thread {
    int port;
    InetAddress address;

    public ClientUdp(String ip, int port){
        try {
            this.port = port ;
            this.address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        DatagramSocket dso = null;
        try{
            dso=new DatagramSocket(port);
            byte[]data=new byte[100];
            while(true){
                DatagramPacket paquet = new DatagramPacket( data, data.length, address, port ) ;
                // Prepare the packet for receive
                paquet.setData( new byte[100] ) ;
                // receive a response from the server
                dso.receive( paquet ) ;
                String st=new String(paquet.getData(),0,paquet.getLength());
                System.out.println("J'ai reçu :"+st);
                System.out.println("De la machine "+paquet.getAddress().toString());
                System.out.println("Depuis le port "+paquet.getPort());
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if (dso != null)
                dso.close();
        }
    }

}