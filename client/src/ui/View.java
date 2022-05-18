package ui;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import model.GameInfo;
import model.MessageInfo;
import model.PlayerModel;
import ui.panels.lobby.WaitPanel;

// unique jframe
public class View extends JFrame {

    private static int defaultWidth;
    private static int defaultHeight;

    JPanel mainPanel;
    LobbyPanel lobbyPanel;
    GamePanel gamePanel;
    WaitPanel waitPanel;
    static View view = null;

    private void switchPanel(JPanel newPanel) {
        Point p = new Point(getLocationOnScreen());
        Dimension oldDim = new Dimension(getSize());
        
        getContentPane().remove(mainPanel);
        mainPanel = newPanel;

        EventQueue.invokeLater(() -> {
            getContentPane().add(mainPanel);
            setSize(mainPanel.getSize());
            Dimension newDim = new Dimension(getSize());
            p.x += (oldDim.width - newDim.width) / 2;
            p.y += (oldDim.height - newDim.height) / 2;
            setLocation(p);
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
        lobbyPanel = new LobbyPanel();
        mainPanel = lobbyPanel;
        getContentPane().add(mainPanel);
        EventQueue.invokeLater(() -> setVisible(true));
    }

    public static void initialize(int width, int height) {
        defaultHeight = height;
        defaultWidth = width;
        if (view == null)
            view = new View();
    }

    public static void initialize() {
        initialize(1000, 600);
    }

    public static View getInstance() {
        return view;
    }

    public static int getDefaultHeight() {
      return defaultHeight;
    }
    
    public static int getDefaultWidth() {
        return defaultWidth;
    }

    public void updateLobbyWindow(List<GameInfo> gameList) {
        EventQueue.invokeLater(() -> {
            lobbyPanel.getGameListPanel().processGameList(gameList);
            revalidate();
        });
    }

    public void incomingMessage(MessageInfo mi) {
        if (gamePanel != null)
            gamePanel.getChatWholePanel().addMessage(mi);
    }

    public void regError() {
        // TODO : show error msg
    }

    public void regOk() {
        waitPanel = new WaitPanel();
        switchPanel(waitPanel);
    }

    public void showGame() {
        // TODO: make sure game infos has been recieved
        gamePanel = new GamePanel(GameInfo.getCurrentGameInfo(), PlayerModel.getCurrentPlayer());
        switchPanel(gamePanel);
    }

    public void ghostMoved(int x, int y) {
        // TODO
    }

    public void ghostCaptured(String username, int points, int x, int y) {
        // TODO
    }

    public void endGameAndShowWinner(String id, int p) {
        // TODO
    }

}
