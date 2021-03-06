package model;

public class GameInfo {

    private int gameID; // The identifiant of the game
    private int playerCount; // The number of players already within this game

    private int labyheight;
    private int labywidth;
    private int nbGhosts;

    private static GameInfo currentGameInfo;

    public GameInfo(int gameID, int playerCount) {
        this(gameID, playerCount, 0, 0);
    }

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

    public int getNbGhosts() {
        return nbGhosts;
    }

    public void setId(int gameID) {
      this.gameID = gameID;
    }

    public void setNbGhosts(int nbGhosts) {
      this.nbGhosts = nbGhosts;
    }

    public String toString() {
        return String.format("Game #%-3d            %3d player(s)", getID(), getPlayerCount());
    }

    public static void setCurrentGameInfo(GameInfo gameInfo) {
      GameInfo.currentGameInfo = gameInfo;
    }

    public static GameInfo getCurrentGameInfo() {
        return GameInfo.currentGameInfo;
    }
}
