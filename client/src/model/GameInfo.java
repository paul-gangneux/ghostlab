package src.model;

public class GameInfo {

    private int gameID; // The identifiant of the game
    private int playerCount; // The number of players already within this game

    public GameInfo(int gameID, int playerCount) {
        this.gameID = gameID;
        this.playerCount = playerCount;
    }

    public int getID() {
        return gameID;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public String toString() {
        return String.format("Game #%d            %d players", getID(), getPlayerCount());
    }
}
