import javafx.scene.control.ListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    MessageType type;
    String message;
    HashMap<String, Integer> loggedInClient;
    int code;
    int move;
    String recipient;
    int[] wl;

    public Message(int i, int connect){
        if(connect == 1) {
            type = MessageType.NEWUSER;
            message = "User "+i+" has joined!";
            code = i;
        }else if(connect == 0){
            type = MessageType.DISCONNECT;
            message = "User "+i+" has disconnected!";
            code = i;
        }
    }

    public Message(String mess, int identifier){
        if(identifier == -1){
            type = MessageType.LOOKINGFORGAME;
            message = mess;
        }else if(identifier == -2){
            type = MessageType.LOGGEDIN;
            message = mess;
        }else{
            type = MessageType.PLAYERMOVE;
            message = mess;
            code = identifier;
        }

    }

    public Message(String rec, String mess){
        if(mess.equals("GAME FOUND")){
            type = MessageType.SERVERMESSAGE;
        }else if(mess.equals("EXIT GAME")){
            type = MessageType.DISCONNECT;
        }else{
            type = MessageType.TEXT;
        }
        message = mess;
        recipient = rec;
    }

    public Message(HashMap<String, Integer> user){
        type = MessageType.USERS;
        loggedInClient = user;
    }

    public Message(String username, String password, int ForC){
        type = MessageType.VALIDNAME;
        message = username + " " + password;
        code = ForC;
    }
    public Message(String username, String password, int ForC, int[] w){
        type = MessageType.VALIDNAME;
        message = username + " " + password;
        code = ForC;
        wl = w;
    }
    public Message(String username, int losses, int wins){
        type = MessageType.UPDATESTATS;
        message = username;
        wl = new int[2];
        wl[0] = losses;
        wl[1] = wins;
    }
    public Message(int c, String mess, int m){
        type = MessageType.PLAYERMOVE;
        code = c;
        move = m;
        message = mess;
    }
    public Message(String mess){
        type = MessageType.SERVERMESSAGE;
        message = mess;
    }
}

