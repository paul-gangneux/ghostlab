import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client extends Thread {
    private Socket server ;
    private int portUdp ;
    private byte[] buffer ;
    private DatagramSocket datagramSocket ;
    private InetAddress inetAddress ;
    private Scanner key; // Scanner for input

    public Client(String ip, int portTcp,int portUdp){
        try {
            server = new Socket(ip, portTcp);
            datagramSocket = new DatagramSocket();
            inetAddress = InetAddress.getByName(ip);
            this.portUdp = portUdp;
            key = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean checkRequest(String request){
        if(request.isEmpty() || request.equals("")){
          System.out.println("ERROR : empty request");
          return false;
        }
        if(!(request.endsWith("***"))){
          System.out.println("ERROR : bad fomulated request");
          return false;
        }
        return true;
    }
    
    @Override
    public void run(){
        DataInputStream istream = null;
        DataOutputStream ostream = null;
        DatagramPacket datagramPacket= null;
        try {
            while(true){
                key = new Scanner(System.in);
                istream = new DataInputStream(server.getInputStream()); 
                ostream = new DataOutputStream(server.getOutputStream());
                
                System.out.println(istream.readUTF());  // Print what the server sends
                System.out.print(">");
                String tosend = key.nextLine();
                if(checkRequest(tosend)){
                    ostream.writeUTF(tosend);   // Send whatever the user typed to the server
                    System.out.println(istream.readUTF());  // read what the server sends before exiting.
                }
            }
            //TODO: when receive the messages through UDP 
        }
         catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        Client thr1 = new Client("localhost",4999,5555);
        thr1.run();
    }
}