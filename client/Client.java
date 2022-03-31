import java.io.*;
import java.net.*;
public class Client {
  static final int port = 8080; 
  /*TODO : verifier si la connection au socket s'est bien effectué 
  et si oui entrer un pseudo.
  Ensuite le serveur doit generer un id et l'attribuer au joueur en question.
  */
  public static void main(String[] args){
    try{
      //TCP pour les requetes etc..
      Socket socket = new Socket("localhost",port);
      
      BufferedReader readBuff = new BufferedReader(
        new InputStreamReader((socket.getInputStream())));
      
        PrintWriter writeBuff = new PrintWriter(
        new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
      //UDP servira uniquement pour les messages privées entre joueurs
      String s = "content*";
      DatagramSocket dso=new DatagramSocket();
      byte[]data;
        data=s.getBytes();
        DatagramPacket paquet=new
        DatagramPacket(
          data,data.length,InetAddress.getByName("localhost"),port);
        dso.send(paquet);
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
}