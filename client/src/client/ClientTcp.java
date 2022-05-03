package client;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ClientTcp extends Thread {
    private Socket server ;
    private Scanner key; // Scanner for input
    DataInputStream istream ;
    PrintWriter ostream ;
    String ogameList ; // we initialize the list of the games available with a string so we can parse it later
    int numberOfGames ;
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
        boolean end = false ;
        try {
            //istream=new BufferedReader(new InputStreamReader(server.getInputStream()));
            istream = new DataInputStream(server.getInputStream()); 
            ostream=new PrintWriter(new OutputStreamWriter(server.getOutputStream()));
            byte[] buf = new byte[128];
            key = new Scanner(System.in);
            

            while(true){
                //ostream = new DataOutputStream(server.getOutputStream());
                //String message = new String (readline(istream), StandardCharsets.UTF_8);
                //getNumberOfGames(reply);
                while(istream.available()>0){
                    int rep = readline(istream, buf);
                }
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
        int id = 0;
        int lu = 0 ;
        String rep="";
        int ch = 0;
        String opcode = getOpCode(data);
        rep+=opcode;

        //we match each opcode and treat it to read the exact amount of bytes
        switch (opcode) {
            case "GAMES":
                for (int i = 0; i < 5; i++) {
                    ch = data.read();
                    lu++;
                    if((byte)ch < 31 || (byte)ch>127){
                        int games = (int)ch ;
                        String gamesNum = String.valueOf(games);
                        this.numberOfGames = games;
                        rep+=gamesNum;
                    }
                    else{
                        rep+=(char)ch;
                    }
                }
                System.out.println(rep);
                return lu;
            case "OGAME":
                for(int j = 0 ; j<7 ; j++){
                    ch = data.read();
                    lu++;
                    if(((byte)ch < 31 || (byte)ch>127)){
                        int num = (int)ch ;
                        if(id == 0){
                            System.out.println("game :"+num);

                            String gameIdString = String.valueOf(num);
                            this.ogameList+=gameIdString+" ";
                            rep+=gameIdString;
                            id=1;
                        }
                        else{
                            System.out.println("players :"+num);
                            String numplayers = String.valueOf(num);
                            this.ogameList+=numplayers+" ";
                            rep+=numplayers;
                            id=1;  
                        }
                    }
                    else{
                        rep+=(char)ch;
                    }
                }
                System.out.println(rep);
                return lu;

            case "REGOK":
                for(int j = 0 ; j<5 ; j++){
                    ch = data.read();
                    lu++;
                    if((byte)ch < 31 || (byte)ch>127){
                        int games = (int)ch ;
                        String gamesNum = String.valueOf(games);
                        this.numberOfGames = games;
                        rep+=gamesNum;
                    }
                    else{
                        rep+=(char)ch;
                    }
                }
                System.out.println(rep);
                return lu;

            case "REGNO":
            for(int j = 0 ; j<4 ; j++){
                ch = data.read();
                lu++;
                rep+=(char)ch;
                
            }
            System.out.println(rep);
            return lu;

            default:
            break;
        }
        return lu;
    }

    //return the first five characters read from the inputstream in string format
    public String getOpCode(DataInputStream data) throws IOException{
        String opcode = "";
        int ch ;
        for (int i = 0; i < 5; i++) {
            ch = data.read();
            opcode+=(char)ch;
        }
        return opcode;
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
        c.start();
    }
}