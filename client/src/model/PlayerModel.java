package model;

import client.Client;

public class PlayerModel {
    // private String name = "default"; à faire ?
    private String name ;
    private int xpos;
    private int ypos;
    private int score;

    static PlayerModel playerModel = null;  

    public static final int MV_UP = 1;
    public static final int MV_LE = 2;
    public static final int MV_RI = 3;
    public static final int MV_DO = 4;

    private PlayerModel(String username ) {
        this.name = username;
        xpos = 0;
        ypos = 0;
        score = 0;
    }

    public void setName(String name){
        this.name = name;
    }

    public  String getPseudo(){
        return name;
    }

    public static PlayerModel getCurrentPlayer() {
        return playerModel;
    }

    public PlayerModel(int x, int y) {
        xpos = x;
        ypos = y;
    }

    private PlayerModel(int x, int y, int initScore) {
        this(x, y);
        score = initScore;
    }

    public static void initialize(String username) {
        if (playerModel == null) playerModel = new PlayerModel(username);
    }

    public synchronized int getXPos() {
        return xpos;
    }

    public synchronized int getYPos() {
        return ypos;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized void setXPos(int value) {
        xpos = value; // TODO : check validity
    }

    public synchronized void setYPos(int value) {
        ypos = value; // TODO : check validity
    }

    public synchronized void setScore(int value) {
        score = value; // TODO : check validity
    }

    public void moveTo(int gridXPos, int gridYPos) {
        int direction = 0;
        int amount = 0;
        if (gridXPos == xpos) {
            amount = gridYPos - ypos;
            if (amount < 0) {
                amount = -amount;
                direction = MV_UP;
            } else {
                direction = MV_DO;
            }
        } else if (gridYPos == ypos) {
            amount = gridXPos - xpos;
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
            Client.getInstance().move(amount, direction);
        }
    }
}
