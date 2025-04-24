//import com.sun.security.ntlm.Client;

public class ClientGames {
    GameLogic currentGame;
    Server.ClientThread playerOne;
    Server.ClientThread playerTwo;
    public ClientGames(Server.ClientThread p1) {
        currentGame = new GameLogic();
        playerOne = p1;
        playerTwo = null;
    }
    public boolean needsPlayer() {
        return playerTwo == null;
    }
    public void addPlayer(Server.ClientThread c) {
        playerTwo = c;
    }

    public Server.ClientThread getPlayerOne() {
        return playerOne;
    }

    public Server.ClientThread getPlayerTwo() {
        return playerTwo;
    }
}
