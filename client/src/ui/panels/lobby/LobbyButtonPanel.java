package ui.panels.lobby;

// import java.awt.event.ActionEvent;
// import java.awt.event.ActionListener;
import java.awt.GridLayout;
import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import client.Client;
import model.PlayerModel;
import ui.LobbyPanel;

public class LobbyButtonPanel extends JPanel {

    private LobbyPanel parentWindow;
    private GridLayout gl;
    private GameCreateButton gcb;
    private GameJoinButton gjb;
    private NameTextField username;
    private JLabel badNameLabel;

    private boolean triedToJoin; // else it's tried to create

    private class NameTextField extends JTextField {

        NameTextField(String text) {
            super(text);
            setFont(getFont().deriveFont(14f));
            getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(DocumentEvent e) {
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (getText().length() > 8) {
                        EventQueue.invokeLater(() -> {
                            setText(getText().substring(0, 8));
                        });
                    } else if (!getText().matches("[a-zA-Z0-9]+")) {
                        EventQueue.invokeLater(() -> {
                            badNameLabel.setText(" user name must be alphanumeric");
                        });
                    } else {
                        EventQueue.invokeLater(() -> {
                            badNameLabel.setText("");
                        });
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (getText().length() == 0) {
                        EventQueue.invokeLater(() -> {
                            badNameLabel.setText(" user name can't be empty");
                        });
                    } else if (!getText().matches("[a-zA-Z0-9]+")) {
                        EventQueue.invokeLater(() -> {
                            badNameLabel.setText(" user name must be alphanumeric");
                        });
                    } else {
                        EventQueue.invokeLater(() -> {
                            badNameLabel.setText("");
                        });
                    }
                }

            });
        }
    }

    private class GameCreateButton extends JButton {

        private GameCreateButton() {
            super();
            setFont(getFont().deriveFont(14f));
            setText("Create game");
            setEnabled(true);
            addActionListener(event -> {
                triedToJoin = false;
                PlayerModel.getCurrentPlayer().setName(username.getText());
                Client.getInstance().createGame(username.getText());
            });
        }
    }

    private class GameJoinButton extends JButton {

        private GameJoinButton() {
            super();
            setFont(getFont().deriveFont(14f));
            setText("Join game");
            setEnabled(false); // A good thing would be to prevent the button from being clicked if no game is
                               // selected
            addActionListener(event -> {
                triedToJoin = true;
                PlayerModel.getCurrentPlayer().setName(username.getText());
                Client.getInstance().joinGame(
                        parentWindow.getGameListPanel().getSelectedGameInfo().getID(),
                        username.getText());
            });
        }
    }

    private class RefreshButton extends JButton {

        private RefreshButton() {
            super();
            setFont(getFont().deriveFont(14f));
            setText("Refresh game list");
            setEnabled(true); // A good thing would be to prevent the button from being clicked if no game is
                              // selected
            addActionListener(event -> {
                gjb.setEnabled(false);
                Client.getInstance().askForGameList();
            });
        }
    }

    public void allowJoining() {
        gjb.setEnabled(true);
    }

    public LobbyButtonPanel(LobbyPanel parentWindow) {
        super();
        triedToJoin = false;
        this.parentWindow = parentWindow;
        gl = new GridLayout(0, 1); // 1 row, 2 columns for the two buttons, 5 px horizontal shift, no vertical
                                   // shift
        setLayout(gl);
        gcb = new GameCreateButton();
        gjb = new GameJoinButton();

        username = new NameTextField(PlayerModel.getCurrentPlayer().getPseudo());
        badNameLabel = new JLabel("");
        badNameLabel.setForeground(Color.RED);
        badNameLabel.setFont(badNameLabel.getFont().deriveFont(14f));
        badNameLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        username.setVisible(true);
        JPanel aPanel = new JPanel();
        aPanel.setLayout(new BoxLayout(aPanel, BoxLayout.X_AXIS));
        JLabel aLabel = new JLabel(" user name:  ");
        aLabel.setFont(aLabel.getFont().deriveFont(14f));
        aPanel.add(aLabel);
        aPanel.add(username);
        // aPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        add(aPanel);
        // add(username);
        add(badNameLabel);
        add(gcb);
        add(gjb);
        add(new RefreshButton());
    }

    public boolean hasTriedToJoin() {
        return triedToJoin;
    }

    public boolean errorWithUsername() {
        return (!badNameLabel.getText().equals(""));
    }

}
