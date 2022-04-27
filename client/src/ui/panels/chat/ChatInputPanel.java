package ui.panels.chat;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import client.Client;
import model.ChatScope;
import model.MessageInfo;
import ui.GameWindow;

public class ChatInputPanel extends JPanel {

    private Client client;

    private class ScopeMenu extends JMenu {
        /*
            Menu bar to choose the scope of the message
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
            ScopeMenuItem teamScope = new ScopeMenuItem("TEAM", ChatScope.TEAM_MSG);
            teamScope.setEnabled(client.hasTeam());
            add(teamScope);
            for (String playerName : client.getAllOtherPlayersNames()) {
                ScopeMenuItem playerWhisper = new ScopeMenuItem(playerName, ChatScope.OUTGOING_PRIVATE_MSG);
                add(playerWhisper);
            }
        }
    }

    private class ChatInputField extends JTextField {
        /*
            Actual input box for the message
        */

        private static final int CHAT_MESSAGE_MAX_LENGTH = 40;

        private void updateChatValidity() {
            System.out.println(getText());
            if (getText().length() == 0) {
                csb.setEnabled(false);
                return;
            }
            if (getText().length() > CHAT_MESSAGE_MAX_LENGTH) {
                setForeground(Color.GRAY);
                csb.setEnabled(false);
                return;
            }
            System.out.println(getText());
            setForeground(Color.BLACK);
            csb.setEnabled(true);
        }

        public ChatInputField() {
            super();
            setBackground(Color.WHITE);
            setForeground(Color.GRAY);
            setText("Enter any message here... (max 40 chars)");
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
        }
    }

    private class ChatSendButton extends JButton {
        /*
            Click and send it.
        */

        public ChatSendButton() {
            super();
            setText("Send");
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    sendChatMessage();
                }
                
            });
        }
    }

    private void sendChatMessage() {
        // Send the actual message... Use cif.getText()
        MessageInfo mi = new MessageInfo(sm.scope, (sm.scope == ChatScope.OUTGOING_PRIVATE_MSG) ? sm.destName : client.getName(), cif.getText());
        // See MessageInfo comments for more info on this ternary
        client.sendOnChat(mi);
        // We then reset the textfield
        cif.setText("Enter any message here... (max 40 chars)");
        cif.setForeground(Color.GRAY);
    }

    private GridLayout gl;

    private GameWindow parentWindow;
    private ScopeMenu sm;
    private ChatInputField cif;
    private ChatSendButton csb;

    public ChatInputPanel(Client c, GameWindow parentWindow) {
        super();
        this.parentWindow = parentWindow;
        client = c;
        gl = new GridLayout(0, 3, 0, 0);
        sm = new ScopeMenu();
        cif = new ChatInputField();
        csb = new ChatSendButton();
        JMenuBar bar = new JMenuBar(); // We make a menu bar to encapsulate the ScopeMenu
        bar.add(sm);
        add(bar);
        add(cif);
        add(csb);
    }
}