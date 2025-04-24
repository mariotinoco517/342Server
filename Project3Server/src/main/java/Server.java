import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

//import com.sun.security.ntlm.Client;
import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	int count = 1;
	//list of all clients including those not logged in
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

	//map of usernames to client numbers
	HashMap<String, Integer> loggedInClient = new HashMap<>();

	//list of ongoing games
	ArrayList<ClientGames> games = new ArrayList<>();
	TheServer server;
	private Consumer<Message> callback;
	
	
	Server(Consumer<Message> call){
	
		callback = call;
		server = new TheServer();
		server.start();
	}
	
	
	public class TheServer extends Thread{
		
		public void run() {

			try(ServerSocket mysocket = new ServerSocket(5555);){
		    	System.out.println("Server is waiting for a client!");
		    	while(true) {
					//creates new clients and adds to clients ArrayList
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept(new Message(count,1));
					clients.add(c);
					c.start();
				
					count++;
			    }
			}//end of try
			catch(Exception e) {
				callback.accept(new Message("Server did not launch", 1));
			}
		}//end of while
	}
	

	class ClientThread extends Thread{
			
		
		Socket connection;
		int count;
		String name;
		ObjectInputStream in;
		ObjectOutputStream out;
			
		ClientThread(Socket s, int count){
			this.connection = s;
			this.count = count;
		}
			
		public void updateClients(Message message)
		{
			switch(message.type){
				case TEXT:
					handeText(message);
					break;
				case NEWUSER:
					//Updates existing clients about new client connection
					handleNewUser(message);
					break;
				case DISCONNECT:
					//tells remaining clients of a disconnected client
					handleDisconnect(message);
					break;
				case USERS:
					//sends array of existing clients to new client
					handleUsers(message);
					break;
				case VALIDNAME:
					handleValidName(message);
					break;

				case PLAYERMOVE:
					//todo implementation of game logic
					break;
				case LOOKINGFORGAME:
					try{
						this.out.writeObject(new Message("SERVER LOOKING"));
					}catch(Exception e){
						System.err.println("VALIDATION ERROR");
					}
					boolean found = false;
					//looks inside games array for a game also looking for players
					//if a game is found add this client to the game
					for(ClientGames curr: games){
						if(curr.needsPlayer() && !curr.getPlayerOne().name.equals(this.name)){
							curr.addPlayer(this);
							try{
								this.out.writeObject(new Message(curr.getPlayerOne().name, "GAME FOUND"));
								curr.getPlayerOne().out.writeObject(new Message(this.name, "GAME FOUND"));
								found = true;
							}catch(Exception e){
								System.err.println("VALIDATION ERROR");
							}

						}
					}
					//when there are no games online or no games are open create a new game
					if(!found){
						games.add(new ClientGames(this));
					}

			}
		}

		public void run(){

			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			}
			catch(Exception e) {
				System.out.println("Streams not open");
			}

			//update existing clients of new client connection
			updateClients(new Message(count,1));

			//gets list of existing clients and send to new client
			updateClients(new Message(loggedInClient));

			while(true) {
				try {
					Message data = (Message) in.readObject();
					callback.accept(data);
					updateClients(data);
				}
				catch(Exception e) {
					e.printStackTrace();
					Message discon = new Message(count, 0);
					callback.accept(discon);
					updateClients(discon);
					clients.remove(this);
					break;
				}
			}
		}//end of run

		public void handleNewUser(Message message){
			for(ClientThread t : clients) {
				if(this != t) {
					try {
						t.out.writeObject(message);
					} catch (Exception e) {
						System.err.println("New User Error");
					}
				}
			}
		}
		public void handleDisconnect(Message message){
			for(ClientThread t : clients) {
				try {
					t.out.writeObject(message);
				} catch (Exception e) {
					System.err.println("New User Error");
				}
			}
		}
		public void handeText(Message message){
			for(ClientThread t: clients){
				//if code is to send to all or threads name matches send text
				if(message.recipient.equals(t.name) ) {
					try {
						message.recipient = this.name;
						t.out.writeObject(message);
					} catch (Exception e) {
						System.err.println("TEXT SEND ERROR");
					}
					break;
				}
			}
		}
		public void handleUsers(Message message){
			for(ClientThread t : clients) {
				if(this == t) {
					try {
						t.out.writeObject(message);
					} catch (Exception e) {
						System.err.println("New User Error");
					}
				}
			}
		}
		public void handleValidName(Message message){
			int code = 0;
			String attemptedUser = message.message.substring(0, message.message.indexOf(" "));
			String attemptedPass = message.message.substring(message.message.indexOf(" ") + 1);

			System.out.println(message.message);
			try{
				File f = new File("src/main/java/Users.txt");
				Scanner myReader = new Scanner(f);

				//default code when attempting to log in with invalid user/password combo
				code = 404;


				//loops through Users.txt to find for same username
				while(myReader.hasNextLine()){
					String line = myReader.nextLine();
					String user = line.substring(0, line.indexOf(" "));

					if(user.equals(attemptedUser)) {
						//client is attempting to create account with existing username
						if(message.code == 0){
							code = 414;
							break;
						}
						String pass = line.substring(line.indexOf(" ") + 1);
						//client gave valid username and password combo when logging in
						if(pass.equals(attemptedPass)) {
							if(message.code == 1){
								loggedInClient.put(attemptedUser, code);
								code = 1;
							}

						}
						else{//client gave valid username but invalid password when logging in
							if(message.code == 1){
								code = -1;
							}
						}
						break;
					}
				}

			}catch(FileNotFoundException e){
				System.err.println("USER FILES NOT FOUND REALLY BIG ISSUE");
			}
			//user is attempting to create an account with a new username
			if(code == 404 && message.code == 0){
				try{
					FileWriter myWriter = new FileWriter("src/main/java/Users.txt", true);
					myWriter.write(attemptedUser + " " + attemptedPass + "\n");
					myWriter.close();
					code = 1;
					loggedInClient.put(attemptedUser, code);
				}catch(IOException e){
					System.err.println("COULDN'T OPEN USERS FILE");
				}
			}
			message.code = code;
			for(ClientThread t : clients) {
				if(this == t) {
					try{
						t.out.writeObject(message);
						t.name = attemptedUser;
					}catch(Exception e){
						System.err.println("VALIDATION ERROR");
					}

				}
				if(this != t){
					try{
						t.out.writeObject(new Message(attemptedUser, 2));
					}catch(Exception e){
						System.err.println("VALIDATION ERROR");
					}
				}
			}
		}

	}//end of client thread

}


	
	

	
