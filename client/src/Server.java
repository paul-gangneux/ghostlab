package src;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread{
    ServerSocket serverSocket;
    public Server(int port){
        try {
            // Create a new Server on specified port.
            serverSocket = new ServerSocket(port);
            // SoTimeout is basiacally the socket timeout.
            // timeout is the time until socket timeout in milliseconds
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void run(){ 
        while(!Thread.interrupted()){
            try {
                // Log with the port number and machine ip
                Logger.getLogger((this.getClass().getName())).log(Level.INFO, "Listening for Clients at {0} on {1}", new Object[]{serverSocket.getLocalPort(), InetAddress.getLocalHost().getHostAddress()});
                Socket client = serverSocket.accept();  // Accept client conncetion
                // Now get DataInputStream and DataOutputStreams
                DataInputStream istream = new DataInputStream(client.getInputStream()); // From client's input stream
                DataOutputStream ostream = new DataOutputStream(client.getOutputStream());
                // Important Note
                /*
                    The server's input is the client's output
                    The client's input is the server's output
                */
                // Send a welcome message
                ostream.writeUTF("Welcome!");
            
                String inString = istream.readUTF();    // Read what the user sent
                System.out.println(inString);
                String outString = inString.toUpperCase();  // Change it to caps
                ostream.writeUTF(outString);
                                           
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static void main(String argv[]) {
        Server thr1 = new Server(4999);
        thr1.run();
    }
}