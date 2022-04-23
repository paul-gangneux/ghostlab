package ui.panels.game;

import java.awt.Color;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;

import client.Client;

public class LabyTile extends JPanel implements MouseInputListener {

    private Client client;

    private int gridXPos;
    private int gridYPos;

    private LabyDisplayerPanel parentPanel;

    private boolean hasFog = false;

    public LabyTile(Client client, LabyDisplayerPanel ldp, int xpos, int ypos) {
        this.client = client;
        parentPanel = ldp;
        gridXPos = xpos;
        gridYPos = ypos;
        setBackground(Color.BLACK); // unrevealed tile
    }

    public void setToWall() {
        setBackground(Color.GRAY);
    }

    public void setEmpty() {
        setBackground(Color.WHITE);
    }

    // TODO : add a transparent mask for the selections and fog

    public void setFog(boolean value) {
        hasFog = value;
        //setOpaque(!value); // if there is fog, we need transparence, so no opacity, and so on
        setBackground(value ? Color.LIGHT_GRAY : null); // TODO : if null color doesn't work, find a workaround !!!
    }

    public void setSelected() {
        //setOpaque(false);
        setBackground(hasFog ? Color.ORANGE : Color.GREEN);
    }

    public void setUnselected() {
        //setOpaque(!hasFog); // if fog, then keep the translucidity, else remove it
        //setBackground(hasFog ? Color.LIGHT_GRAY : null); // TODO : if null color doesn't work, find a workaround !!!
        setBackground(Color.BLACK);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Move To Tile Routine
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        System.out.println(String.format("Entered tile (%d, %d)", gridXPos,  gridYPos));
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
