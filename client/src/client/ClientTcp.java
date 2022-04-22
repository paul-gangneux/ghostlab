package client;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientTcp extends Thread {
    private Socket server ;
    private Scanner key; // Scanner for input

    public ClientTcp(String ip, int portTcp){
        try {
            server = new Socket(ip, portTcp);
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
        try {
            while(true){

                istream = new DataInputStream(server.getInputStream()); 
                ostream = new DataOutputStream(server.getOutputStream());
                String message = readline(istream).toString();
                System.out.println(message);  // Print what the server sends
                key = new Scanner(System.in);
                System.out.print(">");
                String tosend = key.nextLine();
                if(checkRequest(tosend)){
                    ostream.writeUTF(tosend);   // Send whatever the user typed to the server
                    System.out.println(istream.readUTF());  // read what the server sends before exiting.
                }
            }
        }
         catch (IOException e) {
            e.printStackTrace();
        }
    }

     byte[] readline(DataInputStream data){
        byte[] response=new byte[50];
        try{
            int cpt=0;
            while (true) {
                if (cpt==50)return null;
                response[cpt++]=(byte)data.read();
            }
        }catch (IOException e){
            return response;
        }
    }

    public static void main(String[] args) {
        ClientTcp c = new ClientTcp("localhost", 4999);
        c.run();
    }
}