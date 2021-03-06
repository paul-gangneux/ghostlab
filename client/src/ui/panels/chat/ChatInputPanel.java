package ui.panels.chat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import client.Client;
import model.*;

public class ChatInputPanel extends JPanel {

    // private Client client;
    // private GridLayout gl;

    // private GamePanel parentWindow;
    private ScopeMenu sm;
    private ChatInputField cif;
    private ChatSendButton csb;
    private transient MessageInfo lastPmInfo;
    // private ChatWholePanel cwp;

    public static final int CHAT_MESSAGE_MAX_LENGTH = 80;

    private class ScopeMenu extends JMenu {
        /*
         * Menu bar to choose the scope of the message
         */

        ChatScope scope;
        String destName = ""; // Empty since it is everyone by default, so no specific name is specified.

        private class ScopeMenuItem extends JMenuItem implements ActionListener {

            private ChatScope menuItemScope;
            private String displayName;

            public ScopeMenuItem(String displayName, ChatScope componentScope) {
                super(displayName);
                this.displayName = displayName;
                menuItemScope = componentScope;
                addActionListener(this);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                sm.setText(displayName);
                sm.scope = menuItemScope;
                sm.destName = displayName;
            }

        }

        public ScopeMenu() {
            super("ALL");
            scope = ChatScope.GLOBAL_MSG;
            ScopeMenuItem globalScope = new ScopeMenuItem("ALL", ChatScope.GLOBAL_MSG);
            add(globalScope);
            // ScopeMenuItem teamScope = new ScopeMenuItem("TEAM", ChatScope.TEAM_MSG);
            // teamScope.setEnabled(client.hasTeam());
            // add(teamScope);
            for (PlayerModel player : PlayerModel.getAllPlayers()) {
                ScopeMenuItem playerWhisper = new ScopeMenuItem(player.getPseudo(), ChatScope.OUTGOING_PRIVATE_MSG);
                if (!player.getPseudo().equals(PlayerModel.getCurrentPlayer().getPseudo())) {
                    add(playerWhisper);
                }
            }
        }

        public void updateMenu() {
            for (Component c : getComponents()) {
                remove(c);
            }
            for (PlayerModel player : PlayerModel.getAllPlayers()) {
                if (!player.getPseudo().equals(PlayerModel.getCurrentPlayer().getPseudo())) {
                    ScopeMenuItem playerWhisper = new ScopeMenuItem(player.getPseudo(), ChatScope.OUTGOING_PRIVATE_MSG);
                    add(playerWhisper);
                }
            }
        }
    }

    private class ChatInputField extends JTextField {
        /*
         * Actual input box for the message
         */

        private void updateChatValidity() {
            // System.out.println(getText());
            if (getText().length() == 0) {
                csb.setEnabled(false);
                return;
            }
            if (getText().length() > CHAT_MESSAGE_MAX_LENGTH) {
                setForeground(Color.GRAY);
                csb.setEnabled(false);
                return;
            }
            // System.out.println(getText());
            setForeground(Color.BLACK);
            csb.setEnabled(true);
        }

        public ChatInputField() {
            super();
            setBackground(Color.WHITE);
            setForeground(Color.GRAY);
            setText("");
            setColumns(30);
            getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateChatValidity();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateChatValidity();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateChatValidity();
                }

            });
            addKeyListener(new KeyListener() {

                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        csb.sendChatMessage();
                    }
                }

                @Override
                public void keyReleased(KeyEvent e) {
                }

                @Override
                public void keyTyped(KeyEvent e) {
                }

            });
        }
    }

    private class ChatSendButton extends JButton {
        /*
         * Click and send it.
         */

        public ChatSendButton() {
            super();
            setText("Send");
            addActionListener(e -> {
                sendChatMessage();
            });
        }

        private void sendChatMessage() {
            if (cif.getText().length() > 0 && cif.getText().length() <= CHAT_MESSAGE_MAX_LENGTH) {
                // Send the actual message... Use cif.getText()
                MessageInfo mi = new MessageInfo(sm.scope,
                        (sm.scope == ChatScope.OUTGOING_PRIVATE_MSG) ? sm.destName : null,
                        cif.getText());
                // See MessageInfo comments for more info on this ternary

                if (mi.getScope() == ChatScope.GLOBAL_MSG) {
                    Client.getInstance().sendMessToAll(mi.getContent());
                } else if (mi.getScope() == ChatScope.OUTGOING_PRIVATE_MSG) {
                    lastPmInfo = mi;
                    Client.getInstance().sendPrivateMess(mi.getContent(), mi.getPlayerName());
                }
                // We then reset the textfield
                cif.setText("");
                cif.setForeground(Color.GRAY);
            }
        }

    }

    public ChatInputPanel() {
        super();
        this.lastPmInfo = new MessageInfo("");
        sm = new ScopeMenu();
        cif = new ChatInputField();
        csb = new ChatSendButton();
        JMenuBar bar = new JMenuBar(); // We make a menu bar to encapsulate the ScopeMenu
        setLayout(new FlowLayout());
        bar.add(sm);
        add(bar);
        add(cif);
        add(csb);
    }

    public MessageInfo getLastMessageInfo() {
        return lastPmInfo;
    }

    public void updateDests() {
        sm.updateMenu();
    }
}