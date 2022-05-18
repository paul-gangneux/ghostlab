package ui.panels.chat;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

import model.MessageInfo;

public class MessageLabel extends JLabel {

    public MessageLabel(MessageInfo msginfo) {
        super();
        setBackground(Color.WHITE);
        switch (msginfo.getScope()) {
            case OUTGOING_PRIVATE_MSG:
                setForeground(Color.DARK_GRAY);
                setFont(getFont().deriveFont(Font.ITALIC));
                setText("(whispered to " + msginfo.getPlayerName() + ") : " + msginfo.getContent());
                break;
            case OUTGOING_FAILED_PRIVATE_MSG:
                setForeground(Color.RED);
                setFont(getFont().deriveFont(Font.ITALIC));
                setText("(failed to send) : " + msginfo.getContent());
                break;
            case INCOMING_PRIVATE_MSG:
                setForeground(Color.DARK_GRAY);
                setFont(getFont().deriveFont(Font.ITALIC));
                setText("(whisper from " + msginfo.getPlayerName() + ") : " + msginfo.getContent());
                break;
            case GLOBAL_MSG:
                setForeground(Color.BLACK);
                setText(msginfo.getPlayerName() + " : " + msginfo.getContent());
                break;
            case TEAM_MSG:
                setForeground(Color.BLUE);
                setFont(getFont().deriveFont(Font.BOLD));
                setText("(TEAM) " + msginfo.getPlayerName() + " : " + msginfo.getContent());
                break;
            case SERVER_MSG:
                setForeground(Color.GREEN);
                setFont(getFont().deriveFont(Font.BOLD));
                setText("(SERVER) " + msginfo.getContent());
                break;
            default:
                break;
        }
    }
}
