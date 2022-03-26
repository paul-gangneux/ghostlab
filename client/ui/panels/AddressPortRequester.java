package ui.panels;

import java.awt.Color;
import java.awt.Font;

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
                    checkIPFormat();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkIPFormat();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkIPFormat();
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
                setForeground(Color.RED);
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
                    checkPortFormat();
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkPortFormat();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkPortFormat();
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
        }
    }
    
    public AddressPortRequester() {
        super();
        // default layout : flow layout
        add(new IPInputBox());
        add(new PortInputBox());
        add(new ConnectionButton());
    }
}
