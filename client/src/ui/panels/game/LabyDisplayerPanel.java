package ui.panels.game;

// import javax.swing.ImageIcon;
import javax.swing.JPanel;
import model.GameInfo;
import ui.GamePanel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;

public class LabyDisplayerPanel extends JPanel {

    private GamePanel parentWindow;
    private GridLayout gl;

    private LabyTile[][] labyGrid;

    // private int x_pointed = -1;
    // private int y_pointed = -1;

    private ArrayList<int[]> coordPath; // the coords of all tiles that form a straight path from the player to the
                                        // cursor-selected tile

    public LabyDisplayerPanel(GamePanel parentWindow, GameInfo gameinfo, int maxWidth, int maxHeight) {
        super();
        this.parentWindow = parentWindow;
        coordPath = new ArrayList<>();
        int labyHeight = gameinfo.getLabyHeight();
        int labyWidth = gameinfo.getLabyWidth();
        gl = new GridLayout(labyHeight, labyWidth, 0, 0); // 0 px horizontal shift, 0 px vertical shift

        int size1 = maxHeight / labyHeight;
        int size2 = maxWidth / labyWidth;
        int tileSize = (size1 < size2) ? size1 : size2;

        setPreferredSize(new Dimension(tileSize * labyWidth, tileSize * labyHeight));
        setMaximumSize(new Dimension(tileSize * labyWidth, tileSize * labyHeight));
        setLayout(gl);
        LabyTile.initImages();
        LabyTile.resizeImages(tileSize);
        labyGrid = new LabyTile[labyHeight][labyWidth];
        for (int i = 0; i < labyHeight; i++) {
            for (int j = 0; j < labyWidth; j++) {
                LabyTile lt = new LabyTile(this, j, i, tileSize);
                labyGrid[i][j] = lt;
                lt.addMouseListener(lt);
                add(lt);
            }
        }
    }

    public LabyTile[][] getGrid() {
        return labyGrid;
    }

    /*
     * public int[] getPointedTileCoords() { // returns the (x, y) coordinates of
     * the tile currently pointed by the cursor, or (-1, -1) if there is none.
     * int[] answer = new int[2];
     * answer[0] = x_pointed;
     * answer[1] = y_pointed;
     * return answer;
     * }
     */

    public int getMovementLength() {
        return coordPath.size() - 1; // We remove the starting tile (the one where the player is)
    }

    // updates stored cursor position, and selects tiles from
    // the player to the specified one.
    public void makeSelection(int gridXPos, int gridYPos) { 
        // check coord validity ? nah
        int playerX = parentWindow.getPlayerModel().getX();
        int playerY = parentWindow.getPlayerModel().getY();
        if (gridXPos != playerX && gridYPos != playerY) {
            // No straight line from each tiles
            return; // Nothing to do
        }
        synchronized (coordPath) {
            if (gridXPos == playerX) { // case of a vertical selection
                if (gridYPos <= playerY) { // case of a up to down selection
                    for (int i = gridYPos; i < playerY; i++) { // exludes player home time
                        labyGrid[i][gridXPos].setSelected();
                        int[] coords = new int[2];
                        coords[0] = gridXPos;
                        coords[1] = i;
                        coordPath.add(coords);
                    }
                } else { // case of a down to up selection
                    for (int i = playerY + 1; i < gridYPos + 1; i++) { // exludes player home time
                        labyGrid[i][gridXPos].setSelected();
                        int[] coords = new int[2];
                        coords[0] = gridXPos;
                        coords[1] = i;
                        coordPath.add(coords);
                    }
                }
            } else { // case of an horizontal selection
                if (gridXPos <= playerX) { // case of a left to right selection
                    for (int i = gridXPos; i < playerX; i++) { // exludes player home time
                        labyGrid[gridYPos][i].setSelected();
                        int[] coords = new int[2];
                        coords[0] = i;
                        coords[1] = gridYPos;
                        coordPath.add(coords);
                    }
                } else { // case of a right to left selection
                    for (int i = playerX + 1; i < gridXPos + 1; i++) { // exludes player home time
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
        // x_pointed = -1;
        // y_pointed = -1;
    }

}
