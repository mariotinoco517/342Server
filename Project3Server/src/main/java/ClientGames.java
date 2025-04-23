import com.sun.security.ntlm.Client;

public class ClientGames {
    Game currentGame;
    Server.ClientThread playerOne;
    Server.ClientThread playerTwo;
    public ClientGames(Server.ClientThread p1) {
        currentGame = new Game(p1.name);
        playerOne = p1;
        playerTwo = null;
    }
    public boolean needsPlayer() {
        return playerTwo == null;
    }
    public void addPlayer(Server.ClientThread c) {
        playerTwo = c;
    }
}
