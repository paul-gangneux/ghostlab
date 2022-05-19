package ui.panels.lobby;
// import java.awt.*;

import javax.swing.*;
import client.Client;
import ui.View;

public class WaitPanel extends JPanel {
    JButton readyButton;
    JLabel label;

    public WaitPanel() {
        super();
        
        readyButton = new JButton("ready");
        readyButton.addActionListener( event -> {
            Client.getInstance().ready();
            readyButton.setEnabled(false);
        });
        label = new JLabel("Waiting for players");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        setSize(View.getDefaultWidth(), View.getDefaultHeight());

        label.setAlignmentX(CENTER_ALIGNMENT);
        readyButton.setAlignmentX(CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(16f));
        readyButton.setFont(label.getFont().deriveFont(16f));
        add(label);
        add(readyButton);
        
        
    }
}