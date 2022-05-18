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
        // setSize(1000, 600);
        setSize(View.getDefaultWidth(), View.getDefaultHeight());
        // setLayout(new FlowLayout());
        add(readyButton);
        add(label);
        setSize(1000, 600);
    }
}