package ui;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import client.Client;
import model.GameInfo;
import model.PlayerModel;
import ui.panels.lobby.WaitPanel;

// unique jframe
public class View extends JFrame {
    JPanel mainPanel;
    LobbyPanel lobbyp;
    GamePanel gamep;
    WaitPanel waitp;
    static View view = null;

    private void switchPanel(JPanel newPanel) {
        getContentPane().remove(mainPanel);
        mainPanel = newPanel;
        
        EventQueue.invokeLater(() -> {
            getContentPane().add(mainPanel);
            // pack();
            // repaint();
            revalidate();
        });
    }

    private View() {
        setTitle("ghost lab");
        setSize(1000, 600);
        setLocationRelativeTo(null); // centers the window
        // setResizable(false);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        lobbyp = new LobbyPanel();
        mainPanel = lobbyp;
        getContentPane().add(mainPanel);
        EventQueue.invokeLater(() -> setVisible(true));
    }

    public static void initialize() {
        if (view == null)
            view = new View();
    }

    public static View getInstance() {
        initialize();
        return view;
    }

    public void updateLobbyWindow(List<GameInfo> gameList) {
        EventQueue.invokeLater(() -> lobbyp.getGameListPanel().processGameList(gameList));
    }

    public void regError() {
        // TODO : show error msg
    }

    public void regOk() {
        waitp = new WaitPanel();
        switchPanel(waitp);
    }

    public void showGame() {
        //TODO: make sure game infos has been recieved

        PlayerModel pm = new PlayerModel(2, 2); // TODO: get player info
        gamep = new GamePanel(GameInfo.getCurrentGameInfo(),pm);
        switchPanel(gamep);
    }
}
