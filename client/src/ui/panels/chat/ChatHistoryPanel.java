package ui.panels.chat;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;
import client.Client;
import model.*;
import ui.*;

public class ChatHistoryPanel extends JPanel {

    private static final int CHAT_HISTORY_LENGTH = 50;

    private GamePanel parentWindow;
    private GridLayout gl;
    private ArrayList<MessageInfo> messageQueue;
    private int chatPosition = 0;

    private void clearList() {
        for (Component c : getComponents()) {
            remove(c);
        }
    }

    public void dumpChatHistory() {
        System.out.println(getComponentCount());
    }

    public void addMessage(MessageInfo msgInfo) {
        messageQueue.add(msgInfo);
        if (messageQueue.size() < CHAT_HISTORY_LENGTH) {
            add(new MessageLabel(msgInfo));
        }
        else {
            chatPosition++;
            clearList();
            for (int i = chatPosition; i < messageQueue.size(); i++) {
                add(new MessageLabel(messageQueue.get(i))).setVisible(true);
            }
        }
        revalidate(); // recomputes the layout, effectively refreshing the chat
    }

    private ChatWholePanel cwp;

    public ChatHistoryPanel(ChatWholePanel cwp, GamePanel parentWindow) {
        super();
        this.cwp = cwp;
        this.parentWindow = parentWindow;
        gl = new GridLayout(CHAT_HISTORY_LENGTH, 1, 0, 0);
        setLayout(gl);
        messageQueue = new ArrayList<MessageInfo>();
    }
    
}
