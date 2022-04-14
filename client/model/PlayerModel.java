package model;

public class PlayerModel {
    private int xpos;
    private int ypos;
    private int score;

    public PlayerModel() {
        xpos = 0;
        ypos = 0;
        score = 0;
    }

    public PlayerModel(int x, int y) {
        xpos = x;
        ypos = y;
    }

    public PlayerModel(int x, int y, int initScore) {
        this(x, y);
        score = initScore;
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
}
