package src.model;

public class GameInfo {

    private int gameID; // The identifiant of the game
    private int playerCount; // The number of players already within this game

    private int labyheight;
    private int labywidth;

    public GameInfo(int gameID, int playerCount, int labyheight, int labywidth) {
        this.gameID = gameID;
        this.playerCount = playerCount;
        this.labyheight = labyheight;
        this.labywidth = labywidth;
    }

    public int getID() {
        return gameID;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getLabyHeight() {
        return labyheight;
    }

    public int getLabyWidth() {
        return labywidth;
    }

    public String toString() {
        return String.format("Game #%d            %d players", getID(), getPlayerCount());
    }
}
