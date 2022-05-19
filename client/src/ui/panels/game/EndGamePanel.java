package ui.panels.game;
// import java.awt.*;

import javax.swing.*;
import java.awt.*;
import client.Client;
import ui.View;

public class EndGamePanel extends JPanel {
    JButton endButton;
    JLabel label;
    JLabel label2;

    public EndGamePanel(String name, int points) {
        super();

        endButton = new JButton("exit");
        endButton.addActionListener(event -> {
            Client.getInstance().quitting();
        });
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        label = new JLabel("game ended");
        String post = (points > 1 ? "s" : "");
        label2 = new JLabel(name + " won with " + points + " point" + post);
        setSize(View.getDefaultWidth(), View.getDefaultHeight());

        endButton.setAlignmentX(CENTER_ALIGNMENT);
        label.setAlignmentX(CENTER_ALIGNMENT);
        label2.setAlignmentX(CENTER_ALIGNMENT);

        label.setFont(label.getFont().deriveFont(16f));
        label2.setFont(label.getFont().deriveFont(20f));

        add(label, BorderLayout.CENTER);
        add(label2);
        add(endButton);

    }
}