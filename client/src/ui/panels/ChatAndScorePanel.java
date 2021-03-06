package ui.panels;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import client.Client;
import model.MessageInfo;
import ui.GhostCounter;
import ui.panels.chat.ChatWholePanel;
import ui.panels.scoreboard.ScoreboardPanel;

public class ChatAndScorePanel extends JPanel {

    private ChatWholePanel cwp;
    private ScoreboardPanel sp;
    private GhostCounter gc;
    private QuitButton qb; // no getter because why would you want to access a Button ?

    private class QuitButton extends JButton {
    
        public QuitButton() {
            super();
            setText("Quit Game");
            addActionListener(e -> {
                Client.getInstance().quitting();
            });
        }
    }

    public ChatAndScorePanel(int width, int height) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        cwp = new ChatWholePanel((width) / 3, height);
        sp = new ScoreboardPanel();
        gc = new GhostCounter();
        qb = new QuitButton();
        add(cwp);
        add(sp);
        add(gc);
        add(qb);
    }

    public void addMessage(MessageInfo mi) {
        cwp.addMessage(mi);
    }

    public ChatWholePanel getChatWholePanel() {
        return cwp;
    }

    public ScoreboardPanel getScoreboardPanel() {
        return sp;
    }

    public GhostCounter getGhostCounter() {
        return gc;
    }
}
