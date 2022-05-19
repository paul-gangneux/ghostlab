package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import model.GameInfo;
import model.MessageInfo;
import model.PlayerModel;
import ui.panels.game.LabyTile;
import ui.panels.game.LabyTile.TileType;
import ui.panels.lobby.WaitPanel;

// unique jframe
public class View extends JFrame {

    private static int defaultWidth;
    private static int defaultHeight;

    JPanel mainPanel;
    LobbyPanel lobbyPanel;
    GamePanel gamePanel;
    WaitPanel waitPanel;
    static View view = null;

    private void switchPanel(JPanel newPanel) {
        Point p = new Point(getLocationOnScreen());
        Dimension oldDim = new Dimension(getSize());

        getContentPane().remove(mainPanel);
        mainPanel = newPanel;

        EventQueue.invokeLater(() -> {
            getContentPane().add(mainPanel);
            setSize(mainPanel.getSize());
            Dimension newDim = new Dimension(getSize());
            p.x += (oldDim.width - newDim.width) / 2;
            p.y += (oldDim.height - newDim.height) / 2;
            setLocation(p);
            // repaint();
            revalidate();
        });
    }

    private View() {
        setTitle("ghost lab");
        setSize(1000, 600);
        setLocationRelativeTo(null); // centers the window
        // setResizable(false);
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        lobbyPanel = new LobbyPanel();
        mainPanel = lobbyPanel;
        getContentPane().add(mainPanel);
        EventQueue.invokeLater(() -> setVisible(true));
    }

    public static void initialize(int width, int height) {
        defaultHeight = height;
        defaultWidth = width;
        if (view == null)
            view = new View();
    }

    public static void initialize() {
        initialize(1000, 600);
    }

    public static View getInstance() {
        return view;
    }

    public static int getDefaultHeight() {
        return defaultHeight;
    }

    public static int getDefaultWidth() {
        return defaultWidth;
    }

    public void updateLobbyWindow(List<GameInfo> gameList) {
        EventQueue.invokeLater(() -> {
            lobbyPanel.getGameListPanel().processGameList(gameList);
            revalidate();
        });
    }

    public void posit(PlayerModel pm) {
        PlayerModel curr = PlayerModel.getCurrentPlayer();
        curr.setX(pm.getX());
        curr.setY(pm.getY());
        gamePanel.getLabyDisplayerPanel().getGrid()[pm.getY()][pm.getX()]
                .setTile(TileType.MAIN_PLAYER, false);
        PlayerModel.setMoving(false);
    }

    public void move(PlayerModel newPos) {
        PlayerModel curr = PlayerModel.getCurrentPlayer();
        int dx = 0;
        int dy = 0;
        if (curr.getX() == curr.getDesiredX()) {
            if (curr.getY() < curr.getDesiredY()) {
                dy = 1;
            } else if (curr.getY() > curr.getDesiredY()) {
                dy = -1;
            }
        } else if (curr.getY() == curr.getDesiredY()) {
            if (curr.getX() < curr.getDesiredX()) {
                dx = 1;
            } else if (curr.getX() > curr.getDesiredX()) {
                dx = -1;
            }
        }
        LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
        if (dx != 0 || dy != 0) {
            int x, y;
            x = curr.getX();
            y = curr.getY();
            // int error = 0;
            while ((x != newPos.getX() || y != newPos.getY())) {
                grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
                x += dx;
                y += dy;
                // error++;
                // if (error >= 1000) {
                //     System.out.println("Error: at View.move");
                //     break;
                // }
            }

            if (newPos.getX() != curr.getDesiredX() || newPos.getY() != curr.getDesiredY()) {
                x += dx;
                y += dy;
                grid[y][x].setTile(TileType.WALL, false);
            }
        }
        grid[curr.getY()][curr.getX()].setTile(TileType.VISIBLE_EMPTY, false);
        curr.setX(newPos.getX());
        curr.setY(newPos.getY());
        curr.setScore(newPos.getScore());
        grid[newPos.getY()][newPos.getX()].setTile(TileType.MAIN_PLAYER, false);
        PlayerModel.setMoving(false);
    }

    public void incomingMessage(MessageInfo mi) {
        if (gamePanel != null)
            gamePanel.addMessage(mi);
    }

    public void regError() {
        // TODO : show error msg
    }

    public void regOk() {
        waitPanel = new WaitPanel();
        switchPanel(waitPanel);
    }

    public void showGame() {
        // TODO: make sure game infos has been recieved
        gamePanel = new GamePanel(GameInfo.getCurrentGameInfo(), PlayerModel.getCurrentPlayer());
        switchPanel(gamePanel);
    }

    public void showPlayers(){
        for (PlayerModel m : PlayerModel.getOtherPlayers()) {
            gamePanel.getLabyDisplayerPanel().getGrid()[m.getY()][m.getX()].setTile(TileType.MEMORY_ENEMY_PLAYER, false);
        }
    }

    public void ghostMoved(int x, int y) {
        LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
        new Thread(() -> {
            grid[y][x].setTile(TileType.VISIBLE_GHOST, false);
            sleep(2000);
            if  (grid[y][x].getType() == TileType.VISIBLE_GHOST)
                grid[y][x].setTile(TileType.MEMORY_GHOST, false);
            sleep(1000);
            if (grid[y][x].getType() == TileType.MEMORY_GHOST)
                grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
        }).start();
    }

    public void ghostCaptured(String username, int points, int x, int y) {
        if (!username.equals(PlayerModel.getCurrentPlayer().getPseudo())) {
            LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
            new Thread(() -> {
                grid[y][x].setTile(TileType.VISIBLE_ENEMY_PLAYER, false);
                sleep(1000);
                if  (grid[y][x].getType() == TileType.VISIBLE_ENEMY_PLAYER)
                    grid[y][x].setTile(TileType.MEMORY_ENEMY_PLAYER, false);
                sleep(1000);
                if (grid[y][x].getType() == TileType.MEMORY_ENEMY_PLAYER)
                    grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
            }).start();
        }
        //TODO: utiliser points
    }

    public void endGameAndShowWinner(String id, int p) {
        // TODO
    }

    public void updatePlayerLists() {
        gamePanel.getChatAndScorePanel().getScoreboardPanel().updateScores();
        gamePanel.getChatAndScorePanel().getChatWholePanel().getChatInputPanel().updateDests();
    }

    public void privateMessageSuccess() {
        gamePanel.getChatAndScorePanel().getChatWholePanel().lastMsgSuccess();
    }

    public void privateMessageFailure() {
        gamePanel.getChatAndScorePanel().getChatWholePanel().lastMsgFailed();
    }

    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }
}
