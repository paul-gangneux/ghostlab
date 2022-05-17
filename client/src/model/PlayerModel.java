package model;

public class PlayerModel {
    // private String name = "default"; à faire ?
    private String name ;
    private int xpos;
    private int ypos;
    private int score;

    static PlayerModel playerModel = null;  

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
}
