package ui;

import java.awt.*;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import model.GameInfo;

// unique jframe
public class View extends JFrame {
    JPanel mainPanel;
    LobbyWindow lobbyw;
    GameWindow gamew;
    static View view = null;

    private View() {
        setTitle("you just lost the game");
        setSize(1000, 600);
        setLocationRelativeTo(null); // centers the window
        // setResizable(false);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        lobbyw = new LobbyWindow();
        mainPanel = lobbyw;
        add(mainPanel);
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
        EventQueue.invokeLater(() -> lobbyw.getGameListPanel().processGameList(gameList));
    }
}
