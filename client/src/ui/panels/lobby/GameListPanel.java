package ui.panels.lobby;

import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Component;

import model.GameInfo;
import ui.LobbyPanel;

public class GameListPanel extends JPanel {

    private LobbyPanel parentWindow;

    private GridLayout gl;
    private transient GameInfo selectedGameInfo; // GameInfo of the selected game. Null if none is selected.
    
    public GameListPanel(LobbyPanel parentWindow) {
        super();
        this.parentWindow = parentWindow;
        gl = new GridLayout(1, 1, 0, 5); // 1 row, 1 column, no horizontal shift, 5 px vertical shift
        setLayout(gl);
    }

    private void clearList() {
        for (Component c : getComponents()) {
            remove(c);
        }
    }

    public GameInfo getSelectedGameInfo() {
        return selectedGameInfo;
    }

    public void processGameList(List<GameInfo> gameList) {
        // it is expected that the client will have a method to process the [OGAME m s***] requests in a row to produce this ArrayList
        // Do not call this method before initialisation of the GameListPanel
        clearList();
        gl.setRows(gameList.size());
        for (GameInfo gi : gameList) {
            JLabel jl = new JLabel(gi.toString());
            jl.setOpaque(true);
            jl.setBackground(Color.LIGHT_GRAY);
            jl.addMouseListener(new MouseInputListener() {

                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    for (Component c : getComponents()) { // resets other labels to classic text and background color
                        c.setForeground(Color.BLACK);
                        c.setBackground(Color.LIGHT_GRAY);
                    }
                    jl.setForeground(Color.WHITE);
                    jl.setBackground(Color.BLUE);
                    selectedGameInfo = gi;
                    parentWindow.allowGameJoin();
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
            add(jl);
        }
    }
}
