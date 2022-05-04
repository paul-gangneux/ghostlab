package ui.panels.lobby;

// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import client.Client;
import ui.LobbyWindow;

public class LobbyButtonPanel extends JPanel {

    private LobbyWindow parentWindow;
    private GridLayout gl;
    private GameCreateButton gcb;
    private GameJoinButton gjb;

    private class GameCreateButton extends JButton {

        private GameCreateButton() {
            super();
            setText("Create game");
            setEnabled(true);
            addActionListener( event -> {
                Client.getInstance().createGame();
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

    public void allowJoining() {
        gjb.setEnabled(true);
    }

    public LobbyButtonPanel(LobbyWindow parentWindow) {
        super();
        this.parentWindow = parentWindow;
        gl = new GridLayout(1, 2, 5, 0); // 1 row, 2 columns for the two buttons, 5 px horizontal shift, no vertical shift
        setLayout(gl);
        gcb = new GameCreateButton();
        gjb = new GameJoinButton();
        add(gcb);
        add(gjb);
    }
    
}
