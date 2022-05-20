package ui.panels.chat;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import model.*;
// import ui.*;

public class ChatHistoryPanel extends JPanel {

    private static final int CHAT_HISTORY_LENGTH = 80;

    private ArrayList<MessageLabel> messages;

    // private GamePanel parentWindow;
    // private GridLayout gl;
    // private transient ArrayList<MessageInfo> messageQueue;

    // private int chatPosition = 0;

    // private ChatWholePanel cwp;

    public ChatHistoryPanel() {
        super();
        setBackground(Color.WHITE);
        // setForeground(Color.WHITE);
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        messages = new ArrayList<>();
    }

    // private void clearList() {
    // for (Component c : getComponents()) {
    // remove(c);
    // }
    // }

    public void dumpChatHistory() {
        System.out.println(getComponentCount());
    }

    public void addMessage(MessageInfo msgInfo, JScrollPane jsp) {
        JScrollBar sb = jsp.getVerticalScrollBar();
        boolean bottom = (!(sb.isVisible()) || (sb.getValue() == sb.getMaximum() - sb.getHeight()));

        messages.add(new MessageLabel(msgInfo));

        if (messages.size() > CHAT_HISTORY_LENGTH) {
            remove(messages.get(0)); // remove from view
            messages.remove(0); // remove from list
        }

        add(messages.get(messages.size() - 1));

        jsp.validate();
        if (bottom) {
            sb.setValue(sb.getMaximum() - sb.getHeight());
        }
        repaint();
        revalidate(); // recomputes the layout, effectively refreshing the chat
    }

}
