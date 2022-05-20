package ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import ui.panels.lobby.GameListPanel;
import ui.panels.lobby.LobbyButtonPanel;

public class LobbyPanel extends JPanel {

    private static final int DEFAULT_LAUNCHER_WINDOW_WIDTH = 1000;
    private static final int DEFAULT_LAUNCHER_WINDOW_HEIGHT = 600;

    private GameListPanel glp;
    private LobbyButtonPanel lbp;
    private JPanel rightPanel;
    private JScrollPane scrollPane;
    private JLabel gameInfoLabel;

    private transient List<String> gameUserList;
    private int gameId;
    private int gameW;
    private int gameH;

    public LobbyPanel() {
        this(DEFAULT_LAUNCHER_WINDOW_WIDTH, DEFAULT_LAUNCHER_WINDOW_HEIGHT);
    }

    public LobbyPanel(int width, int height) {
        gameUserList = new ArrayList<>();
        gameId = -1;
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setLayout(new GridLayout(1, 0));
        glp = new GameListPanel(this);
        glp.setBackground(Color.GRAY);
        glp.setOpaque(true);
        scrollPane = new JScrollPane(glp);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        //scrollPane.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1));
        add(scrollPane);
        glp.setScrollPane(scrollPane);

        lbp = new LobbyButtonPanel(this);
        gameInfoLabel = new JLabel("<html></html>");
        gameInfoLabel.setFont(gameInfoLabel.getFont().deriveFont(14f));
        gameInfoLabel.setVerticalAlignment(SwingConstants.TOP);
        gameInfoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 6));
        //gameInfoLabel.setBackground(Color.GRAY);
        gameInfoLabel.setOpaque(true);
        //gameInfoLabel.setForeground(Color.BLUE);
        

        rightPanel = new JPanel(new GridLayout(0, 1));
        rightPanel.add(lbp);
        rightPanel.add(gameInfoLabel);

        add(rightPanel);
        setSize(width, height);
    }

    public void updateGameInfoLabel() {
        gameInfoLabel.setForeground(Color.BLACK);
        if (gameId < 0) {
            gameInfoLabel.setText("<html></html>");
        } else {
            StringBuilder bld = new StringBuilder();
            bld.append("<html>Game id: " + gameId + "<br><br>" +
                    "Size: " + gameW + " x " + gameH + "<br><br>" +
                    "Players:<br><br>");
            for (String p : gameUserList) {
                bld.append("<p style=\"margin-left:20\">" + p + " </p>");
            }
            bld.append("</html>");
            gameInfoLabel.setText(bld.toString());
        }
        EventQueue.invokeLater(() -> {
            repaint();
            revalidate();
        });
    }

    public void allowGameJoin() {
        lbp.allowJoining();
    }

    public GameListPanel getGameListPanel() {
        return glp;
    }

    public void updateUserList(List<String> userList) {
        gameUserList = userList;
        updateGameInfoLabel();
    }

    public void updateGameInfos(int id, int w, int h) {
        gameId = id;
        gameW = w;
        gameH = h;
        //updateGameInfoLabel();
    }

    public void resetGameInfoPanel() {
        gameInfoLabel.setForeground(Color.BLACK);
        gameInfoLabel.setText("<html></html>");
        EventQueue.invokeLater(() -> {
            repaint();
            revalidate();
        });
    }

    public void regError() {
        gameInfoLabel.setForeground(Color.RED);

        String errmsg;
        if (lbp.hasTriedToJoin()) {
            errmsg = "<html>ERROR: Cannot join game<br>";
        } else {
            errmsg = "<html>ERROR: Cannot create game<br>";
        }
        if (lbp.errorWithUsername()) {
            errmsg += "<br>Bad username.</html>";
        } else {
            errmsg += "<br>Maybe your username is already taken,<br>or the game is no longer available.</html>";
        }
        gameInfoLabel.setText(errmsg);
        EventQueue.invokeLater(() -> {
            repaint();
            revalidate();
        });
    }
}
