package client;

import java.util.ArrayList;

import model.MessageInfo;
import ui.GameWindow;
import ui.View;

public class Client {

    private View view; // good way to do

    private GameWindow gw; // bad way to do (for tests)

    public void setGameWindow(GameWindow gw) {
        this.gw = gw;
    }

    public ArrayList<String> getAllOtherPlayersNames() {
        ArrayList<String> res = new ArrayList<String>();
        // TODO : this is used for tests, please make it real !
        for (int i = 1; i < 10; i++) {
            res.add("Player " + Integer.toString(i));
        }
        return res;
    }

    public String getName() {
        return "Uly";
    }

    public boolean hasTeam() {
        return true; // TODO
    }

    public void sendOnChat(MessageInfo mi) {
        gw.addMessage(mi);
    }

    static ClientTcp c1;
    static ClientUdp c2;
    public static void main(String[] args) {
        c1 = new ClientTcp("localhost", 4242);
        c2 = new ClientUdp("localhost", 5555);

        c1.run();
        c2.run();
    }
}
