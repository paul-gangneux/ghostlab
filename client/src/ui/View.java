package ui;

import java.awt.*;
// import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import client.Client;
import launcher.Launcher;
import model.ChatScope;
import model.GameInfo;
import model.MessageInfo;
import model.PlayerModel;
import ui.panels.game.EndGamePanel;
import ui.panels.game.LabyTile;
import ui.panels.game.LabyTile.TileType;
import ui.panels.lobby.WaitPanel;

// unique jframe
public class View extends JFrame {

    private static int defaultWidth;
    private static int defaultHeight;

    private static int fadeout = 2000;
    private static int fadeoutMemory = 2000;

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
            repaint();
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

    public static void setFadeout(int millis) {
        fadeout = millis;
    }

    public static void setFadeoutMem(int millis) {
        fadeoutMemory = millis;
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
            // revalidate();
        });
        lobbyPanel.resetGameInfoPanel();
    }

    public void posit(PlayerModel pm) {
        PlayerModel curr = PlayerModel.getCurrentPlayer();
        curr.setX(pm.getX());
        curr.setY(pm.getY());
        curr.setName(pm.getPseudo());
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
                // System.out.println("Error: at View.move");
                // break;
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
        Client.getInstance().askForLight();
    }

    public void incomingMessage(MessageInfo mi) {
        if (gamePanel != null)
            gamePanel.addMessage(mi);
    }

    public void regError() {
        lobbyPanel.regError();
    }

    public void regOk() {
        waitPanel = new WaitPanel();
        switchPanel(waitPanel);
    }

    public void showGame() {
        gamePanel = new GamePanel(GameInfo.getCurrentGameInfo(), PlayerModel.getCurrentPlayer());
        switchPanel(gamePanel);
    }

    public void showPlayers() {
        LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
        for (PlayerModel m : PlayerModel.getAllPlayers()) {
            if (!m.getPseudo().equals(PlayerModel.getCurrentPlayer().getPseudo())) {
                new Thread(() -> {
                    int x = m.getX();
                    int y = m.getY();
                    grid[y][x].setTile(TileType.VISIBLE_ENEMY_PLAYER, false);
                    sleep(2000);
                    if (grid[y][x].getType() == TileType.VISIBLE_ENEMY_PLAYER)
                        grid[y][x].setTile(TileType.MEMORY_ENEMY_PLAYER, false);
                    sleep(1000);
                    if (grid[y][x].getType() == TileType.MEMORY_ENEMY_PLAYER)
                        grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
                }).start();
            }
        }
    }

    public void ghostMoved(int x, int y) {
        LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
        new Thread(() -> {
            grid[y][x].setTile(TileType.VISIBLE_GHOST, false);
            sleep(fadeout);
            if (grid[y][x].getType() == TileType.VISIBLE_GHOST)
                grid[y][x].setTile(TileType.MEMORY_GHOST, false);
            sleep(fadeoutMemory);
            if (grid[y][x].getType() == TileType.MEMORY_GHOST)
                grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
        }).start();
    }

    public void ghostCaptured(String username, int points, int x, int y) {
        if (!username.equals(PlayerModel.getCurrentPlayer().getPseudo())) {
            LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
            new Thread(() -> {
                grid[y][x].setTile(TileType.VISIBLE_ENEMY_PLAYER, false);
                sleep(2000);
                if (grid[y][x].getType() == TileType.VISIBLE_ENEMY_PLAYER)
                    grid[y][x].setTile(TileType.MEMORY_ENEMY_PLAYER, false);
                sleep(1000);
                if (grid[y][x].getType() == TileType.MEMORY_ENEMY_PLAYER)
                    grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
            }).start();
        }
        PlayerModel pm = PlayerModel.getPlayerByName(username);
        // int nbCaptured = points - pm.getScore();
        pm.setScore(points);
        gamePanel.getChatAndScorePanel().getScoreboardPanel().updateScores();
        gamePanel.getChatAndScorePanel().getGhostCounter().decrease(1); // there's 1 message per capture
        gamePanel.addMessage(new MessageInfo(ChatScope.SERVER_MSG, null, username + " captured a ghost!"));
    }

    public void endGameAndShowWinner(String id, int p) {
        switchPanel(new EndGamePanel(id, p));
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

    public void endGame() {
        lobbyPanel = new LobbyPanel();
        switchPanel(lobbyPanel);
        synchronized (Launcher.waitObj) {
            Launcher.waitObj.notifyAll();
        }
    }

    // 012
    // 3 4
    // 567
    public void lightSurroundings(String lightValues) {
        int x = PlayerModel.getCurrentPlayer().getX();
        int y = PlayerModel.getCurrentPlayer().getY();
        LabyTile[][] grid = gamePanel.getLabyDisplayerPanel().getGrid();
        setTyleAccordingToCharAtInd(grid, lightValues, x - 1, y - 1, 0);
        setTyleAccordingToCharAtInd(grid, lightValues, x, y - 1, 1);
        setTyleAccordingToCharAtInd(grid, lightValues, x + 1, y - 1, 2);
        setTyleAccordingToCharAtInd(grid, lightValues, x - 1, y, 3);
        setTyleAccordingToCharAtInd(grid, lightValues, x + 1, y, 4);
        setTyleAccordingToCharAtInd(grid, lightValues, x - 1, y + 1, 5);
        setTyleAccordingToCharAtInd(grid, lightValues, x, y + 1, 6);
        setTyleAccordingToCharAtInd(grid, lightValues, x + 1, y + 1, 7);

    }

    private void setTyleAccordingToCharAtInd(LabyTile[][] grid, String val, int x, int y, int ind) {
        if (val.charAt(ind) == '1') {
            if (grid[y][x].getType() == TileType.UNKNOWN) {
                grid[y][x].setTile(TileType.WALL, false);
            }
        } else if (val.charAt(ind) == '0') {
            if (grid[y][x].getType() == TileType.UNKNOWN) {
                grid[y][x].setTile(TileType.VISIBLE_EMPTY, false);
            }
        }
    }

    public void showUserListForSectedGame(List<String> userList) {
        lobbyPanel.updateUserList(userList);
    }

    public void showGameInfosForSelectedGame(int id, int h, int w) {
        lobbyPanel.updateGameInfos(id, w, h);
    }

    public void backToLobby() {
        lobbyPanel = new LobbyPanel();
        switchPanel(lobbyPanel);
        Client.getInstance().askForGameList();
    }
}
