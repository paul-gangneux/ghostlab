package client;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

public class ClientTcp extends Thread {
    private Socket server ;
    private Scanner key; // Scanner for input
    DataInputStream istream ;
    PrintWriter ostream ;
    String gameList ; // we initialize the list of the games available with a string so we can parse it later
    int numberOfGames ;

    public static int x = 0 ;
    public static int taille = 0;
    
    public ClientTcp(String ip, int portTcp){
        try {
            server = new Socket(ip, portTcp);
            key = new Scanner(System.in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getNumberOfGames (String reply){//get the server message GAMES n*** THE NUMBER OF GAMES
            if(reply.startsWith("GAMES")){
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

    public String command_start(){
        return "START***";
    }

    public String ask_players_list(int m){
        return "LIST? "+m+"***";
    }

    public String ask_unreg_pl_game_list(){
        return "GAME?***";
    }
    
    public void connect(Socket socket) throws IOException{
        this.server = socket ;
        istream = new DataInputStream(socket.getInputStream()); 
        ostream=new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

    }
    @Override
    public void run(){
        try {
            //istream=new BufferedReader(new InputStreamReader(server.getInputStream()));
            istream = new DataInputStream(server.getInputStream()); 
            ostream=new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
            byte[] buf = new byte[128];
            key = new Scanner(System.in);
            while(true){
                //ostream = new DataOutputStream(server.getOutputStream());
                //String message = new String (readline(istream), StandardCharsets.UTF_8);

                    int rep = readline(istream, buf);
                
                //getNumberOfGames(reply);
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

    //Our readline method
    public int readline(DataInputStream data, byte[] buf) throws IOException{        
        String rep="";
        int star = 0;
        if(x==taille){
            taille = data.read(buf,0,128);
            x = 0;
        }

        for(int i = 0 ; i<taille ;i++){
            int c = buf[i];
            if(star ==3){
                System.out.println(rep);
                return taille;
            }
            if(c=='*'){
                star++;
            }
            if (c < 31 || c > 127) {
                int games = (int) buf[6];
                String gamesNum = String.valueOf(games);
                rep+=gamesNum;
            }
            rep+=(char)buf[i];
        }
        System.out.println(rep);
        return taille;
    }

    public void count_to_go_to_line(byte[] buf,DataInputStream dis) throws IOException{
        int response = dis.read(buf,0,3);
        int stars = 0 ;
        for (byte b: buf) {
            if(stars==3){
                System.out.println();
                break;
            }
            if ((char)b == '*'){
                stars++;
            }
        }
    }
    //get the local port
    public int getPort(){
        return this.server.getLocalPort();
    }

    //get the local inet adress
    public InetAddress getAdress(){
        return this.server.getLocalAddress();
    }

    public static void main(String[] args) {
        ClientTcp c = new ClientTcp("localhost", 4242);
        c.run();
    }
}