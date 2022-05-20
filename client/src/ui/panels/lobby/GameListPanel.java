package ui.panels.lobby;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

import client.Client;

// import client.Client;

import java.awt.*;


import model.GameInfo;
import ui.LobbyPanel;

public class GameListPanel extends JPanel {

    private LobbyPanel parentWindow;
    private List<GameLabel> labelList;

    // private GridLayout gl;
    private transient GameInfo selectedGameInfo; // GameInfo of the selected game. Null if none is selected.

    
    private transient Border glBorder;
    private Dimension glSize;
    private Dimension glMinSize;

    private JScrollPane scrollPane; // parentWindow
    
    public GameListPanel(LobbyPanel parentWindow) {
        super();
        this.parentWindow = parentWindow;
        // setPreferredSize(new Dimension(parentWindow.getWidth(), parentWindow.getHeight()));
        //setLayout(new GridLayout(0,1));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // 1 row, 1 column, no horizontal shift, 5 px vertical shift
        labelList = new ArrayList<>();
        
        glBorder = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
    }

    public void setScrollPane(JScrollPane scrollPane) {
        this.scrollPane = scrollPane;
    }

    private class GameLabel extends JLabel {
        private transient GameInfo gameInfo;

        GameLabel(GameInfo gi) {
            super(gi.toString());
            gameInfo = gi;
            setOpaque(true);
            setBackground(Color.LIGHT_GRAY);
            setPreferredSize(glSize);
            setMaximumSize(glSize);
            setMinimumSize(glMinSize);

            setBorder(glBorder);
            setFont(this.getFont().deriveFont(14f));

            addMouseListener(new MouseInputListener() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (GameLabel c : labelList) { // resets other labels to classic text and background color
                        c.setForeground(Color.BLACK);
                        c.setBackground(Color.LIGHT_GRAY);
                    }
                    setForeground(Color.WHITE);
                    setBackground(Color.BLUE);
                    selectedGameInfo = gameInfo;
                    parentWindow.allowGameJoin(); 
                    Client.getInstance().updateGameInfos(gameInfo.getID());
                }
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {      
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {     
                }
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {       
                }
                @Override
                public void mouseReleased(java.awt.event.MouseEvent e) {          
                }
                @Override
                public void mouseDragged(java.awt.event.MouseEvent e) {             
                }
                @Override
                public void mouseMoved(java.awt.event.MouseEvent e) {   
                } 
            });
        }
        
    }

    private void clearList() {
        for (GameLabel gl : labelList) {
            remove(gl);
        }
        labelList.clear();
    }

    public GameInfo getSelectedGameInfo() {
        return selectedGameInfo;
    }

    public void processGameList(List<GameInfo> gameList) {
        clearList();

        int w = scrollPane.getWidth();
        // w = (w * 9) / 10;
        int h = scrollPane.getHeight();
        glSize = new Dimension(w, h / 20);
        glMinSize = new Dimension(w / 2, h / 20);

        for (GameInfo gi : gameList) {
            labelList.add(new GameLabel(gi));
        }
        for (GameLabel gl : labelList) {
            add(gl);
        }
        repaint();
        revalidate();
    }

    
}
