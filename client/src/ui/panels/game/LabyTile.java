package ui.panels.game;

import java.awt.*;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import model.PlayerModel;

public class LabyTile extends JPanel implements MouseInputListener {

    public enum TileType {
        MAIN_PLAYER,
        //TEAM_PLAYER,
        VISIBLE_ENEMY_PLAYER,
        MEMORY_ENEMY_PLAYER,
        VISIBLE_GHOST,
        MEMORY_GHOST,
        VISIBLE_EMPTY,
        MEMORY_EMPTY,
        //BONUS,
        WALL,
        UNKNOWN
    }

    private int gridXPos;
    private int gridYPos;

    private LabyDisplayerPanel parentPanel;
    private TileType tileType;
    private JLabel icon;
    private int tileSize;

    public LabyTile(LabyDisplayerPanel ldp, int xpos, int ypos, int size) {
        setLayout(new GridLayout(1,1));
        setMinimumSize(new Dimension(10, 10));
        tileSize = size;
        setPreferredSize(new Dimension(size, size));
        setMaximumSize(new Dimension(size, size));
        parentPanel = ldp;
        gridXPos = xpos;
        gridYPos = ypos;
        setTile(TileType.UNKNOWN, false);
    }

    private void clearTile() {
        if (icon != null) {
            remove(icon);
        }
        icon = null;
    }

    public void setTile(TileType type, boolean selected) {
        clearTile();
        tileType = type;
        if (selected) {
            switch (type) {
                case VISIBLE_EMPTY:
                    icon = new JLabel(new ImageIcon("client/resources/empty_tile_selected.png"));
                    break;
                case MEMORY_EMPTY:
                    icon = new JLabel(new ImageIcon("client/resources/empty_tile_selected.png"));
                    break;
                case MAIN_PLAYER:
                    // main character shouldn't be selected, wtf ?
                    icon = new JLabel(new ImageIcon("client/resources/main_character_facing_left_selected.png"));
                    break;
                case MEMORY_ENEMY_PLAYER:
                    icon = new JLabel(new ImageIcon("client/resources/enemy_character_facing_left_selected.png"));   
                    break;
                case MEMORY_GHOST:
                    icon = new JLabel(new ImageIcon("client/resources/memory_ghost_selected.png"));
                    break;
                case UNKNOWN:
                    icon = new JLabel(new ImageIcon("client/resources/unknown_tile_selected.png"));
                    break;
                case VISIBLE_ENEMY_PLAYER:
                    icon = new JLabel(new ImageIcon("client/resources/enemy_character_facing_left_selected.png"));
                    break;
                case VISIBLE_GHOST:
                    icon = new JLabel(new ImageIcon("client/resources/spotted_ghost_selected.png"));
                    break;
                case WALL:
                    icon = new JLabel(new ImageIcon("client/resources/wall_selected.png"));
                    break;
            }
        }
        else {
            switch (type) {
                case VISIBLE_EMPTY:
                    icon = new JLabel(new ImageIcon("client/resources/empty_tile.png"));
                    break;
                case MEMORY_EMPTY:
                    icon = new JLabel(new ImageIcon("client/resources/empty_tile_memory.png"));
                    break;
                case MAIN_PLAYER:
                    icon = new JLabel(new ImageIcon("client/resources/main_character_facing_left.png"));
                    break;
                case MEMORY_ENEMY_PLAYER:
                    icon = new JLabel(new ImageIcon("client/resources/enemy_character_facing_left_memory.png"));   
                    break;
                case MEMORY_GHOST:
                    icon = new JLabel(new ImageIcon("client/resources/memory_ghost.png"));
                    break;
                case UNKNOWN:
                    icon = new JLabel(new ImageIcon("client/resources/unknown_tile.png"));
                    break;
                case VISIBLE_ENEMY_PLAYER:
                    icon = new JLabel(new ImageIcon("client/resources/enemy_character_facing_left.png"));
                    break;
                case VISIBLE_GHOST:
                    icon = new JLabel(new ImageIcon("client/resources/spotted_ghost.png"));
                    break;
                case WALL:
                    icon = new JLabel(new ImageIcon("client/resources/wall.png"));
                    break;
            }
        }
        icon.setSize(tileSize, tileSize);
        add(icon);
        revalidate();
    }

    public void setSelected() {
        setTile(tileType, true);
    }

    public void setUnselected() {
        setTile(tileType, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        PlayerModel.getCurrentPlayer().moveTo(gridXPos,  gridYPos);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // System.out.println(String.format("Entered tile (%d, %d) of type " + tileType.name(), gridXPos,  gridYPos)); // TODO : Remove this for final version please
        parentPanel.makeSelection(gridXPos, gridYPos);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        parentPanel.clearSelection();
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
    
}
