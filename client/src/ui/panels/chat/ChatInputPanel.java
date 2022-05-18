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
    // private ChatWholePanel cwp;

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
            ScopeMenuItem teamScope = new ScopeMenuItem("TEAM", ChatScope.TEAM_MSG);
            // teamScope.setEnabled(client.hasTeam());
            add(teamScope);
            // TODO get all player names
            String[] placeholder = { "bob", "alice" };
            for (String playerName : placeholder) {
                ScopeMenuItem playerWhisper = new ScopeMenuItem(playerName, ChatScope.OUTGOING_PRIVATE_MSG);
                add(playerWhisper);
            }
        }
    }

    private class ChatInputField extends JTextField {
        /*
         * Actual input box for the message
         */

        private static final int CHAT_MESSAGE_MAX_LENGTH = 80;

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
            // Send the actual message... Use cif.getText()
            MessageInfo mi = new MessageInfo(sm.scope,
                    (sm.scope == ChatScope.OUTGOING_PRIVATE_MSG) ? sm.destName : null,
                    cif.getText());
            // cwp.addMessage(mi);
            // See MessageInfo comments for more info on this ternary
            Client.getInstance().sendMessToAll(mi.getContent());
            // We then reset the textfield
            cif.setText("");
            cif.setForeground(Color.GRAY);
        }

    }

    public ChatInputPanel(/* ChatWholePanel cwp, GamePanel parentWindow */) {
        super();
        // this.cwp = cwp;
        // this.parentWindow = parentWindow;
        // gl = new FlowLayout();
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

}