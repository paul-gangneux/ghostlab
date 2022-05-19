package ui;

import javax.swing.JButton;

import client.ClientTcp;

public class QuitButton extends JButton {
    
    public QuitButton() {
        super();
        setText("Quit Game");
        addActionListener(e -> {
            quitGame();
        });
    }

    private void quitGame() {
        ClientTcp.sendToServer("IQUIT***");
    }

}
