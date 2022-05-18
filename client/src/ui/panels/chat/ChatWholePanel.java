package ui.panels.chat;

import javax.swing.*;

import java.awt.*;
import model.*;

public class ChatWholePanel extends JPanel {

    // private GamePanel parentWindow;
    private JScrollPane jsp;
    private ChatHistoryPanel chp;
    private ChatInputPanel cip;

    public ChatWholePanel(int w, int h) {
        super();
        // this.parentWindow = parentWindow;

        chp = new ChatHistoryPanel();
        cip = new ChatInputPanel();
        jsp = new JScrollPane(chp);
        jsp.setPreferredSize(new Dimension(w, h / 2));
        jsp.setMaximumSize(new Dimension(w, h / 2));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(jsp, BorderLayout.PAGE_START);
        add(cip, BorderLayout.PAGE_END);
    }

    public void addMessage(MessageInfo mi) {
        chp.addMessage(mi, jsp);
    }

    public ChatHistoryPanel getChatHistoryPanel() {
        return chp;
    }

    public ChatInputPanel getChatInputPanel() {
        return cip;
    }

    public void lastMsgSuccess() {
        MessageInfo mi = cip.getLastMessageInfo();
        mi.setScope(ChatScope.OUTGOING_PRIVATE_MSG);
        chp.addMessage(mi, jsp);
    }

    public void lastMsgFailed() {
        MessageInfo mi = cip.getLastMessageInfo();
        mi.setScope(ChatScope.OUTGOING_FAILED_PRIVATE_MSG);
        chp.addMessage(mi, jsp);
    }

}
