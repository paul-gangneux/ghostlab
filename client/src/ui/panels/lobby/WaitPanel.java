package ui.panels.lobby;
// import java.awt.*;

// import java.awt.Color;

import javax.swing.*;
import client.Client;
import ui.View;

public class WaitPanel extends JPanel {
    JButton readyButton;
    JButton quitButton;
    JLabel label;

    public WaitPanel() {
        super();
        
        readyButton = new JButton("ready");
        quitButton = new JButton("quit");

        readyButton.addActionListener( event -> {
            Client.getInstance().ready();
            readyButton.setEnabled(false);
            quitButton.setEnabled(false);
        });

        quitButton.addActionListener( event -> {
            Client.getInstance().unReg();
        });
        
        label = new JLabel("Waiting for players");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        setSize(View.getDefaultWidth(), View.getDefaultHeight());

        label.setAlignmentX(CENTER_ALIGNMENT);
        readyButton.setAlignmentX(CENTER_ALIGNMENT);
        quitButton.setAlignmentX(CENTER_ALIGNMENT);

        label.setFont(label.getFont().deriveFont(16f));
        readyButton.setFont(label.getFont().deriveFont(16f));
        quitButton.setFont(label.getFont().deriveFont(16f));
        // setBackground(Color.BLUE);

        add(label);
        add(readyButton);
        add(quitButton);
    }
}