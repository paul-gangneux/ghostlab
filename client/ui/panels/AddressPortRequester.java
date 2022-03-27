package ui.panels;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


public class AddressPortRequester extends JPanel {

    private class IPInputBox extends JTextField {
        private IPInputBox() {
            setText("Enter IP address here");
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

        private PortInputBox() {
            setText("Enter port here");
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

    private class ConnectionButton extends JButton {

        private ConnectionButton() {
            super();
            setText("Connect");
            setEnabled(false);
            addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Connection routine : TODO
                }
                
            });
        }

    }

    private IPInputBox inputBox;
    private PortInputBox portInputBox;
    private ConnectionButton connectionButton;
    
    public AddressPortRequester() {
        super();
        // default layout : flow layout
        inputBox = new IPInputBox();
        portInputBox = new PortInputBox();
        connectionButton = new ConnectionButton();
        add(inputBox);
        add(portInputBox);
        add(connectionButton);
    }

    private void updateConnectionValidity() {
        boolean canConnect = inputBox.checkIPFormat() && portInputBox.checkPortFormat();
        // Due to java being a lazy language, the second test is only run when the first one is passed,
        // leading to the port changing color only when the address is valid. This is not a bug.
        // However, it is possible to change this behavior if wanted.
        connectionButton.setEnabled(canConnect);
    }
}
