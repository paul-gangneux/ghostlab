package ui.panels.game;

import javax.swing.JPanel;

import client.Client;
import model.GameInfo;
import ui.GamePanel;

import java.awt.GridLayout;
import java.util.ArrayList;

public class LabyDisplayerPanel extends JPanel {

    private GamePanel parentWindow;
    private GridLayout gl;

    private LabyTile[][] labyGrid;

    private int x_pointed = -1;
    private int y_pointed = -1;

    private ArrayList<int[]> coordPath; // the coords of all tiles that form a straight path from the player to the cursor-selected tile

    public LabyTile[][] getGrid() {
        return labyGrid;
    }

    /* public int[] getPointedTileCoords() { // returns the (x, y) coordinates of the tile currently pointed by the cursor, or (-1, -1) if there is none.
        int[] answer = new int[2];
        answer[0] = x_pointed;
        answer[1] = y_pointed;
        return answer;
    } */

    public int getMovementLength() {
        return coordPath.size() - 1; // We remove the starting tile (the one where the player is)
    }

    public void makeSelection(int gridXPos, int gridYPos) { // updates stored cursor position, and selects tiles from the player to the specified one.
        // TODO : check coord validity ?
        int playerX = parentWindow.getPlayerModel().getXPos();
        int playerY = parentWindow.getPlayerModel().getYPos();
        if (gridXPos != playerX && gridYPos != playerY) {
            // No straight line from each tiles
            return; // Nothing to do
        }
        synchronized (coordPath) {
            if (gridXPos == playerX) { // case of a vertical selection
                if (gridYPos <= playerY) { // case of a up to down selection
                    for (int i = gridYPos; i < playerY + 1; i++) { // +1 includes player home tile
                        labyGrid[i][gridXPos].setSelected();
                        int[] coords = new int[2];
                        coords[0] = gridXPos;
                        coords[1] = i;
                        coordPath.add(coords);
                    }
                }
                else { // case of a down to up selection
                    for (int i = playerY; i < gridYPos + 1; i++) { // +1 includes player home tile
                        labyGrid[i][gridXPos].setSelected();
                        int[] coords = new int[2];
                        coords[0] = gridXPos;
                        coords[1] = i;
                        coordPath.add(coords);
                    }
                }
            }
            else { // case of an horizontal selection
                if (gridXPos <= playerX) { // case of a left to right selection
                    for (int i = gridXPos; i < playerX + 1; i++) { // +1 includes player home tile
                        labyGrid[gridYPos][i].setSelected();
                        int[] coords = new int[2];
                        coords[0] = i;
                        coords[1] = gridYPos;
                        coordPath.add(coords);
                    }
                }
                else { // case of a right to left selection
                    for (int i = playerX; i < gridXPos + 1; i++) { // +1 includes player home tile
                        labyGrid[gridYPos][i].setSelected();
                        int[] coords = new int[2];
                        coords[0] = i;
                        coords[1] = gridYPos;
                        coordPath.add(coords);
                    }
                }
            }
        }
    }

    public void clearSelection() { // unsets the coordinates of the selected tile and the array of selected tiles
        synchronized (coordPath) {
            for (int[] coords : coordPath) {
                labyGrid[coords[1]][coords[0]].setUnselected();
            }
            coordPath.clear();
        }
        x_pointed = -1;
        y_pointed = -1;
    }
    
    public LabyDisplayerPanel(GamePanel parentWindow, GameInfo gameinfo) {
        super();
        this.parentWindow = parentWindow;
        coordPath = new ArrayList<>();
        int labyHeight = gameinfo.getLabyHeight();
        int labyWidth = gameinfo.getLabyWidth();
        gl = new GridLayout(labyHeight, labyWidth, 0, 0); // 1 px horizontal shift, 1 px vertical shift
        setLayout(gl);
        labyGrid = new LabyTile[labyHeight][labyWidth];
        for (int i = 0; i < labyHeight; i++) {
            for (int j = 0; j < labyWidth; j++) {
                LabyTile lt = new LabyTile(this, j, i);
                labyGrid[i][j] = lt;
                lt.addMouseListener(lt);
                add(lt);
            }
        }
    }
}
