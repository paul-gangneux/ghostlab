package ui.panels.lobby;

// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import client.Client;
import model.PlayerModel;
import ui.LobbyWindow;
import ui.View;

public class LobbyButtonPanel extends JPanel {

    private LobbyWindow parentWindow;
    private GridLayout gl;
    private GameCreateButton gcb;
    private GameJoinButton gjb;
    private JTextField username;
    private JTextField udpPort;


    private class GameCreateButton extends JButton {

        private GameCreateButton() {
            super();
            setText("Create game");
            setEnabled(true);
            addActionListener( event -> {
                PlayerModel.initialize(username.getText());
                Client.getInstance().createGame(username.getText());
                View.initialize();
            });
        }
    }

    private class GameJoinButton extends JButton {

        private GameJoinButton() {
            super();
            setText("Join game");
            setEnabled(false); // A good thing would be to prevent the button from being clicked if no game is selected
            addActionListener( event -> {
                // Game joining routine : TODO
                // Here we have to gather the selectedGameInfo from the GameListPanel of the LobbyWindow
                // -> parentWindow.getGameListPanel().getSelectedGameInfo()
            });
        }
    }

    private class RefreshButton extends JButton {

        private RefreshButton() {
            super();
            setText("refresh games");
            setEnabled(true); // A good thing would be to prevent the button from being clicked if no game is selected
            addActionListener( event -> {
                View.getInstance();
            });
        }
    }

    public void allowJoining() {
        gjb.setEnabled(true);
    }

    public LobbyButtonPanel(LobbyWindow parentWindow) {
        super();
        this.parentWindow = parentWindow;
        gl = new GridLayout(4, 1, 5, 0); // 1 row, 2 columns for the two buttons, 5 px horizontal shift, no vertical shift
        setLayout(gl);
        gcb = new GameCreateButton();
        gjb = new GameJoinButton();
        username = new JTextField("enter pseudo");
        username.setVisible(true);
        udpPort = new JTextField("enter port");
        udpPort.setVisible(true);
        add(username);
        add(gcb);
        add(gjb);
    }
    
}
