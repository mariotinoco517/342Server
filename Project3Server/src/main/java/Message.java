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
    String recipient;

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
        if(identifier == 1){
            type = MessageType.LOOKINGFORGAME;
            message = mess;
        }else if(identifier == 2){
            type = MessageType.LOGGEDIN;
            message = mess;
        }

    }

    public Message(String rec, String mess){
        if(mess.equals("GAME FOUND")){
            type = MessageType.SERVERMESSAGE;
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

    public Message(String mess){
        type = MessageType.SERVERMESSAGE;
        message = mess;
    }
}

