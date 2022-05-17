package ui.panels.chat;

import java.awt.GridLayout;

import javax.swing.JPanel;

import client.Client;
import model.*;
import ui.*;

public class ChatWholePanel extends JPanel {
    
    private GridLayout gl;

    private GamePanel parentWindow;
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

    public ChatWholePanel(GamePanel parentWindow) {
        super();
        this.parentWindow = parentWindow;
        gl = new GridLayout(2, 1, 0, 0);
        chp = new ChatHistoryPanel(this, parentWindow);
        cip = new ChatInputPanel(this, parentWindow);
        add(chp);
        add(cip);
    }

}
