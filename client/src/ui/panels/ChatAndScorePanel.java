package ui.panels;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import model.MessageInfo;
import ui.panels.chat.ChatWholePanel;
import ui.panels.scoreboard.ScoreboardPanel;

public class ChatAndScorePanel extends JPanel {

    ChatWholePanel cwp;
    ScoreboardPanel sp;

    public ChatAndScorePanel(int width, int height) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        cwp = new ChatWholePanel((width) / 3, height);
        sp = new ScoreboardPanel();
        add(cwp);
        add(sp);
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
}
