import javafx.scene.control.ListView;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;
    MessageType type;
    String message;
    ArrayList<Integer> users;
    int recipient;

    public Message(int i, boolean connect){
        if(connect) {
            type = MessageType.NEWUSER;
            message = "User "+i+" has joined!";
            recipient = i;
        } else {
            type = MessageType.DISCONNECT;
            message = "User "+i+" has disconnected!";
            recipient = i;
        }
    }

    public Message(String mess){
        type = MessageType.TEXT;
        message = mess;
        recipient = -1;
    }

    public Message(int rec, String mess){
        type = MessageType.TEXT;
        message = mess;
        recipient = rec;
    }

    public Message(ArrayList<Integer> user){
        type = MessageType.USERS;
        users = user;
    }
}

