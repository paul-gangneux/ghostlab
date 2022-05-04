package client;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
// import java.util.Scanner;
import model.GameInfo;
import ui.View;

public class ClientTcp extends Thread {
    private Socket server;
    // private Scanner key; // Scanner for input
    BufferedReader istream;
    PrintWriter ostream;
    String ogameList; // we initialize the list of the games available with a string so we can parse it later
    // int numberOfGames; // ???

    public ClientTcp(String ip, int portTcp) {
        try {
            server = new Socket(ip, portTcp);
            // key = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("\nConnexion refusée. assurez-vous que le serveur a bien été lancé");
            System.exit(1);
        }
        try {
            istream = new BufferedReader(new InputStreamReader(server.getInputStream()));
            ostream = new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /* 
    public void getNumberOfGames (String reply){ //get the server message GAMES n*** THE NUMBER OF GAMES
        if(reply.startsWith("GAMES")) {
            String parts[] = reply.split("/");
            String number = parts[0].replace("***","");
            System.out.println(parts[0]);
            if(number.equals(" ")){
                this.numberOfGames = 0 ;
            }
            this.numberOfGames = Integer.parseInt(number);
            System.out.println("games : "+this.numberOfGames);
        }
    }
    */

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

    /*
    public String command_start(){
        return "START***";
    }

    public String ask_players_list(int m){
        return "LIST? "+m+"***";
    }

    public String ask_unreg_pl_game_list(){
        return "GAME?***";
    }
    */
    
    // public void connect(Socket socket) throws IOException{
    //     this.server = socket ;
    //     //istream = new DataInputStream(socket.getInputStream()); 
    //     ostream = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

    // }

    @Override
    public void run() {
        // boolean end = false;
        byte[] buf = new byte[128];
        // key = new Scanner(System.in);
            
        while(true) {
            
            int size = 0;
            try {
                size = readMessage(istream, buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String keyword = getKeyword(buf);

            // pour débugger, à supprimer plus tard
            System.out.println(keyword);

            switch (keyword) {
                case "GAMES": // [GAMES n***]
                    if (size != 10) {
                        System.out.println("wrong size, discarding");
                        break;
                    }
                    int n = buf[6];
                    ArrayList<GameInfo> info = new ArrayList<>();
                    for (int i = 0; i < n; i++) {
                        try {
                            // expecting [OGAME m s***]
                            size = readMessage(istream, buf);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } 
                        keyword = getKeyword(buf);
                        
                        if (size != 12 || !keyword.equals("OGAME")) {
                            System.out.println("error at game info reading");
                            break;
                        }
                        int gameid = buf[6];
                        int plyrcount = buf[8];
                        info.add(new GameInfo(gameid, plyrcount));
                    }
                    View.getInstance().updateLobbyWindow(info);
                    break;

                case "REGOK": // [REGOK m***]
                    // do stuff
                    break;

                case "REGNO": // [REGOK***]
                    // do stuff
                    break;
            
                default:
                    System.out.println("message non compris");
                    break;
            }


            // while(istream.available() > 0) {
                
            // }
            // System.out.print(">");
            // String tosend = key.nextLine();
            // if(checkRequest(tosend)){
            //     ostream.print(tosend);   // Send whatever the user typed to the server
            //     ostream.flush();
            // }
        }
        
    }

    // return true on success, false on failure
    public boolean sendToServer(byte[] data) {
        try {
            server.getOutputStream().write(data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // return true on success, false on failure
    public boolean sendToServer(String s) {
        try {
            server.getOutputStream().write(s.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // reads a single message ending with *** and stores it in buf.
    // returns the number of bytes read
    public int readMessage(BufferedReader in, byte[] buf) throws IOException {
        int star = 0;
        int n = 0;
        int i = 0;

        while(i < buf.length) {
            n = in.read();
            if (n == -1) 
                break;
            buf[i] = (byte)n;
            i++;
            if (n == '*') star++;
            else star = 0;
            if (star == 3) {
                // morceau de code qui permet d'éviter le problème du byte = 42 avant ***
                if (in.ready()) {
                    // equivalent à un peek
                    in.mark(1);
                    n = in.read();
                    in.reset();
                    if (n == '*') {
                        n = in.read();
                        buf[i] = (byte)n;
                        i++;
                        // on peut lire jusqu'à 2 fois 42 avant un ***
                        in.mark(1);
                        n = in.read();
                        in.reset();
                        if (n == '*') {
                            n = in.read();
                            buf[i] = (byte)n;
                            i++;
                        }
                    }
                }
                break;
            }
        }
        return i;
    }

    //return the first five characters read from the inputstream in string format
    public String getKeyword(byte[] buf) {
        byte[] keyBytes = new byte[5];
        for (int i = 0; i < 5; i++)
            keyBytes[i] = buf[i];
        return new String(keyBytes, StandardCharsets.UTF_8);
    }

    // public void count_to_go_to_line(byte[] buf,DataInputStream dis) throws IOException{
    //     int response = dis.read(buf,0,3);
    //     int stars = 0 ;
    //     for (byte b: buf) {
    //         if(stars==3){
    //             System.out.println();
    //             break;
    //         }
    //         if ((char)b == '*'){
    //             stars++;
    //         }
    //     }
    // }

    //get the local port
    public int getPort(){
        return this.server.getLocalPort();
    }

    //get the local inet adress
    public InetAddress getAdress(){
        return this.server.getLocalAddress();
    }
}