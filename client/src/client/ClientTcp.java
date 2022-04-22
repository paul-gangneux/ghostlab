package client;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
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
        DataInputStream istream ;
        PrintWriter ostream ;
        try {
            //istream=new BufferedReader(new InputStreamReader(server.getInputStream()));
            istream = new DataInputStream(server.getInputStream()); 
            ostream=new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
            byte[] buf = new byte[128];
            while(true){
                //ostream = new DataOutputStream(server.getOutputStream());
                //String message = new String (readline(istream), StandardCharsets.UTF_8);

                int size = readline(istream, buf);
                System.out.println(new String(buf, StandardCharsets.UTF_8));  // Print what the server sends
                key = new Scanner(System.in);
                System.out.print(">");
                String tosend = key.nextLine();
                if(checkRequest(tosend)){
                    ostream.print(tosend);   // Send whatever the user typed to the server
                    ostream.flush();
                }
            }
        }
         catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int readline(DataInputStream data, byte[] buf) throws IOException{        
        return data.read(buf,0,128);
    }

    public static void main(String[] args) {
        ClientTcp c = new ClientTcp("localhost", 4242);
        c.run();
    }
}