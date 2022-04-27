package ui.panels.chat;

import java.awt.GridLayout;

import javax.swing.JPanel;

import client.Client;
import model.MessageInfo;
import ui.GameWindow;

public class ChatWholePanel extends JPanel {
    
    private GridLayout gl;

    private GameWindow parentWindow;
    private ChatHistoryPanel chp;
    private ChatInputPanel cip;

    public void addMessage(MessageInfo mi) {
        chp.addMessage(mi);
    }

    public ChatHistoryPanel getChatHistoryPanel() {
        return chp;
    }

    public ChatInputPanel getChatInputPanel() {
        return cip;
    }

    public ChatWholePanel(Client c, GameWindow parentWindow) {
        super();
        this.parentWindow = parentWindow;
        gl = new GridLayout(2, 1, 0, 0);
        chp = new ChatHistoryPanel(c, parentWindow);
        cip = new ChatInputPanel(c, parentWindow);
        add(chp);
        add(cip);
    }

}
