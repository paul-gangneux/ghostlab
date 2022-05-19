package ui.panels.game;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import model.PlayerModel;

public class LabyTile extends JPanel implements MouseInputListener {

    private static Map<String, ImageIcon> images = new HashMap<>();

    public enum TileType {
        MAIN_PLAYER,
        // TEAM_PLAYER,
        VISIBLE_ENEMY_PLAYER,
        MEMORY_ENEMY_PLAYER,
        VISIBLE_GHOST,
        MEMORY_GHOST,
        VISIBLE_EMPTY,
        MEMORY_EMPTY,
        // BONUS,
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
        setLayout(new GridLayout(1, 1));
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

    public static void initImages() {
        images.put("empty", new ImageIcon("client/resources/empty_tile.png"));
        images.put("empty_memory", new ImageIcon("client/resources/empty_tile_memory.png"));
        images.put("empty_selected", new ImageIcon("client/resources/empty_tile_selected.png"));
        images.put("unknown", new ImageIcon("client/resources/unknown_tile.png"));
        images.put("unknown_selected", new ImageIcon("client/resources/unknown_tile_selected.png"));
        images.put("wall", new ImageIcon("client/resources/wall.png"));
        images.put("wall_selected", new ImageIcon("client/resources/wall_selected.png"));
        images.put("main_char", new ImageIcon("client/resources/main_character_facing_left.png"));
        images.put("main_char_selected", new ImageIcon("client/resources/main_character_facing_left_selected.png"));
        images.put("enemy_char", new ImageIcon("client/resources/enemy_character_facing_left.png"));
        images.put("enemy_char_memory", new ImageIcon("client/resources/enemy_character_facing_left_memory.png"));
        images.put("enemy_char_selected", new ImageIcon("client/resources/enemy_character_facing_left_selected.png"));
        images.put("ghost", new ImageIcon("client/resources/spotted_ghost.png"));
        images.put("ghost_memory", new ImageIcon("client/resources/memory_ghost.png"));
        images.put("ghost_memory_selected", new ImageIcon("client/resources/memory_ghost_selected.png"));
        images.put("ghost_selected", new ImageIcon("client/resources/spotted_ghost_selected.png"));
        images.put("error", new ImageIcon("client/resources/error.png"));
    }

    public static void resizeImages(int size) {
        for (ImageIcon img : images.values()) {
            Image image = img.getImage();
            img.setImage(image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH));
            // Image newimg = image.getScaledInstance(size, size, java.awt.Image.SCALE_SMOOTH);
            // img = new ImageIcon(newimg);
        }
    }

    public synchronized void setTile(TileType type, boolean selected) {
        clearTile();
        tileType = type;
        if (selected) {
            switch (type) {
                case VISIBLE_EMPTY:
                    icon = new JLabel(images.get("empty_selected"));
                    break;
                case MEMORY_EMPTY:
                    icon = new JLabel(images.get("empty_selected"));
                    break;
                case MAIN_PLAYER:
                    // main character shouldn't be selected, wtf ?
                    icon = new JLabel(images.get("main_char_selected"));
                    break;
                case MEMORY_ENEMY_PLAYER:
                    icon = new JLabel(images.get("enemy_char_selected"));
                    break;
                case MEMORY_GHOST:
                    icon = new JLabel(images.get("ghost_memory_selected"));
                    break;
                case UNKNOWN:
                    icon = new JLabel(images.get("unknown_selected"));
                    break;
                case VISIBLE_ENEMY_PLAYER:
                    icon = new JLabel(images.get("enemy_char_selected"));
                    break;
                case VISIBLE_GHOST:
                    icon = new JLabel(images.get("ghost_selected"));
                    break;
                case WALL:
                    icon = new JLabel(images.get("wall_selected"));
                    break;
                default:
                    icon = new JLabel(images.get("error"));
                    break;
            }
        } else {
            switch (type) {
                case VISIBLE_EMPTY:
                    icon = new JLabel(images.get("empty"));
                    break;
                case MEMORY_EMPTY:
                    icon = new JLabel(images.get("empty_memory"));
                    break;
                case MAIN_PLAYER:
                    icon = new JLabel(images.get("main_char"));
                    break;
                case MEMORY_ENEMY_PLAYER:
                    icon = new JLabel(images.get("enemy_char_memory"));
                    break;
                case MEMORY_GHOST:
                    icon = new JLabel(images.get("ghost_memory"));
                    break;
                case UNKNOWN:
                    icon = new JLabel(images.get("unknown"));
                    break;
                case VISIBLE_ENEMY_PLAYER:
                    icon = new JLabel(images.get("enemy_char"));
                    break;
                case VISIBLE_GHOST:
                    icon = new JLabel(images.get("ghost"));
                    break;
                case WALL:
                    icon = new JLabel(images.get("wall"));
                    break;
                default:
                    icon = new JLabel(images.get("error"));
                    break;
            }
        }
        icon.setSize(tileSize, tileSize);
        add(icon);
        EventQueue.invokeLater(() -> {
            repaint();
            revalidate();
        });
    }

    public void setSelected() {
        setTile(tileType, true);
    }

    public void setUnselected() {
        setTile(tileType, false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        PlayerModel.getCurrentPlayer().moveTo(gridXPos, gridYPos);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // System.out.println(String.format("Entered tile (%d, %d) of type " +
        // tileType.name(), gridXPos, gridYPos)); // TODO : Remove this for final
        // version please
        parentPanel.makeSelection(gridXPos, gridYPos);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        parentPanel.clearSelection();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public TileType getType() {
        return tileType;
    }

}
