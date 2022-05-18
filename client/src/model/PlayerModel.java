package model;

import java.util.ArrayList;

import client.Client;

public class PlayerModel {

    static ArrayList<PlayerModel> otherPlayers = new ArrayList<>();

    // private String name = "default"; à faire ?
    private String name;
    private int x;
    private int y;
    private int score;

    static PlayerModel currentPlayer = null;

    public static final int MV_UP = 1;
    public static final int MV_LE = 2;
    public static final int MV_RI = 3;
    public static final int MV_DO = 4;
    private static boolean isMoving;

    private int desiredX = 0;
    private int desiredY = 0;

    public PlayerModel(String username, int posX, int posY) {
        this.name = username;
        x = posX;
        y = posY;
        score = 0;
    }

    private PlayerModel(String username) {
        this.name = username;
        x = 0;
        y = 0;
        score = 0;
    }

    public static void addPlayer(PlayerModel pm) {
        // Watch out, no duplicate check is performed here.
        otherPlayers.add(pm);
    }

    public static ArrayList<PlayerModel> getOtherPlayers() {
        return otherPlayers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void setMoving(boolean isMoving) {
      PlayerModel.isMoving = isMoving;
    }

    public static boolean isMoving() {
      return isMoving;
    }

    public String getPseudo() {
        return name;
    }

    public int getDesiredX() {
      return desiredX;
    }

    public int getDesiredY() {
      return desiredY;
    }

    public static PlayerModel getCurrentPlayer() {
        return currentPlayer;
    }

    public PlayerModel(int posX, int posY) {
        x = posX;
        y = posY;
    }

    public PlayerModel(int x, int y, int initScore) {
        this(x, y);
        score = initScore;
    }

    public PlayerModel(String id, int x, int y, int initScore) {
        this(x, y);
        score = initScore;
        name = id;
    }

    public static void initialize(String username) {
        if (currentPlayer == null)
            currentPlayer = new PlayerModel(username);
    }

    public synchronized int getX() {
        return x;
    }

    public synchronized int getY() {
        return y;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized void setX(int value) {
        x = value; // TODO : check validity
    }

    public synchronized void setY(int value) {
        y = value; // TODO : check validity
    }

    public synchronized void setScore(int value) {
        score = value; // TODO : check validity
    }

    public void moveTo(int gridx, int gridy) {
        
        int direction = 0;
        int amount = 0;
        if (gridx == x) {
            amount = gridy - y;
            if (amount < 0) {
                amount = -amount;
                direction = MV_UP;
            } else {
                direction = MV_DO;
            }
        } else if (gridy == y) {
            amount = gridx - x;
            if (amount < 0) {
                amount = -amount;
                direction = MV_LE;
            } else {
                direction = MV_RI;
            }
        } else {
            return;
        }
        if (amount > 0) {
            synchronized(currentPlayer) {
                if (isMoving)
                    return;
                setMoving(true);
            }
            desiredX = gridx;
            desiredY = gridy;
            Client.getInstance().move(amount, direction);
        }
    }
}
