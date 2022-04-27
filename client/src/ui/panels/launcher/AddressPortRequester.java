package ui.panels.launcher;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import client.Client;


public class AddressPortRequester extends JPanel {

    private Client client;

    private class IPInputBox extends JTextField {
        private IPInputBox() {
            // TODO : allow "localhost" string / device address
            setText("127.0.0.1");
            setFont(new Font("arial", 1, 50)); // name, style, size. Style defines if it's bold, italic, etc.
            getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }
                
            });
        }

        private boolean checkIPFormat() {
            String text = getText();
            boolean answer = text.matches("(\\d{1,3}\\.){3}\\d{1,3}");
            if (answer) {
                setForeground(Color.GREEN);
            }
            else {
                if (text.matches("\\d?\\d?(\\d?|\\d\\.(\\d?\\d?(\\d?|\\d\\.(\\d?\\d?(\\d?|\\d\\.(\\d?\\d?(\\d?|\\d)))))))")) { // IP being written
                    setForeground(Color.BLACK);
                }
                else {
                    setForeground(Color.RED);
                }
            }
            return answer;
        }
    }

    private class PortInputBox extends JTextField {

        private static final int DEFAULT_PORT = 4242;

        private PortInputBox() {
            setText(Integer.toString(DEFAULT_PORT));
            setFont(new Font("arial", 1, 50)); // name, style, size. Style defines if it's bold, italic, etc.
            setEditable(true);
            setVisible(true);
            getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }
                
            });
        }

        private boolean checkPortFormat() {
            String text = getText();
            boolean answer;
            try {
                int port = Integer.parseInt(text);
                answer = 0 < port && port <= 65535;
            }
            catch (NumberFormatException nbe) {
                setForeground(Color.RED);
                answer = false;
            }
            answer = answer && text.matches("\\d{1,5}");
            if (answer) {
                setForeground(Color.GREEN);
            }
            else {
                setForeground(Color.RED);
            }
            return answer;
        }
    }

    private class PseudoInputBox extends JTextField {

        private PseudoInputBox() {
            setText("no name");
            setFont(new Font("arial", 1, 50)); // name, style, size. Style defines if it's bold, italic, etc.
            setEditable(true);
            setVisible(true);
            getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateConnectionValidity();
                }
                
            });
        }

        public boolean checkPseudoFormat() {
            int l = getText().length();
            boolean answer = 0 < l && l <= 8;
            if (answer) {
                setForeground(Color.GREEN);
            }
            else {
                setForeground(Color.RED);
            }
            return answer;
        }
    }

    private class ConnectionButton extends JButton {

        private ConnectionButton() {
            super();
            setText("Connect");
            setEnabled(true);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Connection routine : TODO
                    // Don't hesitate to add a client field in the button subclasses if needed.
                }
                
            });
        }

    }

    private IPInputBox inputBox;
    private PortInputBox portInputBox;
    private PseudoInputBox pseudoInputBox;
    private ConnectionButton connectionButton;
    
    public AddressPortRequester(Client client) {
        super();
        this.client = client;
        // default layout : flow layout
        inputBox = new IPInputBox();
        portInputBox = new PortInputBox();
        pseudoInputBox = new PseudoInputBox();
        connectionButton = new ConnectionButton();
        add(inputBox);
        add(portInputBox);
        add(pseudoInputBox);
        add(connectionButton);
    }

    private void updateConnectionValidity() {
        boolean canConnect = inputBox.checkIPFormat() && portInputBox.checkPortFormat() && pseudoInputBox.checkPseudoFormat();
        // Due to java being a lazy language, the second test is only run when the first one is passed,
        // leading to the port changing color only when the address is valid. This is not a bug.
        // However, it is possible to change this behavior if wanted.
        connectionButton.setEnabled(canConnect);
    }
}
