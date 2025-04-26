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
		int wins;
		int losses;
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
					handleMove(message);
					break;
				case LOOKINGFORGAME:
					handleLookingForGame(message);
					break;
				case CHANGENAME:
					handleChangeName(message);
				case REMATCH:
					handleRematch(message);
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
					updateWinLoss();
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
						System.out.println("Server has a new client #" + count);
					} catch (Exception e) {
						System.err.println("New User Error");
					}
				}
			}
		}
		public void handleDisconnect(Message message){
			if(message.message.equals("EXIT GAME")){
				for(ClientThread t : clients) {
					if(t.name.equals(message.recipient)){
						try{
							System.out.println(name + " left the game giving " + t.name + " the win");
							//tells opponent they won
							t.out.writeObject(new Message("WINNER"));
							t.wins++;
							//tells client that exited that they lose
							this.out.writeObject(new Message("LOSER"));
							losses++;
							//deletes game from the games arrayList since it is over
							for(int i = games.size() - 1; i >= 0; i--){
								if(games.get(i).getPlayerOne().name.equals(t.name) || games.get(i).getPlayerTwo().name.equals(t.name)){
//									if(games.get(i).getPlayerOne().name.equals(name) || games.get(i).getPlayerTwo().name.equals(name)){
										games.remove(i);
//									}
								}
							}
						}catch(Exception e){
							System.err.println("EXIT GAME ERROR");
						}

					}
				}
			}else{
				for(ClientThread t : clients) {
					try {
						t.out.writeObject(message);
					} catch (Exception e) {
						System.err.println("New User Error");
					}
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
						System.out.println(name + " sent a text saying '" + message.message + "' to " + t.name);
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
						System.out.println("Client #" + t.count + " got a list of users currently online");
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

			System.err.println("Attempting to validate user for client #" + count);
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
							System.out.println("Client #" + count + " tried to make an account with a taken username");
							code = 414;
							break;
						}
						String pass = line.substring(line.indexOf(" ") + 1);
						//client gave valid username and password combo when logging in
						if(pass.equals(attemptedPass)) {
							if(message.code == 1){
								System.out.println("Client #" + count + " logged into account with username " + attemptedUser);
								loggedInClient.put(attemptedUser, code);
								code = 1;
							}

						}
						else{//client gave valid username but invalid password when logging in
							if(message.code == 1){
								System.out.println("Client #" + count + " couldn't log into account with username " + attemptedUser);
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
					System.out.println("Client #" + count + " created a new account with username " + attemptedUser);
				}catch(IOException e){
					System.err.println("COULDN'T OPEN USERS FILE");
				}
			}
			message.code = code;
			for(ClientThread t : clients) {
				if(this == t) {
					//tell client if its username and password was valid
					try{
						int[] winLoss = getWinLoss(attemptedUser);
						losses = winLoss[0];
						wins = winLoss[1];
						t.out.writeObject(message);
						t.out.writeObject(new Message(attemptedUser, attemptedPass, code, winLoss));
						t.name = attemptedUser;
					}catch(Exception e){
						System.err.println("VALIDATION ERROR");
					}

				}
				if(this != t){
					//tell other clients of a new logged in client
					try{
						if(code == 1){
							System.out.println("Told client #" + t.count + " that client #" + count + " has username " + attemptedUser);
							t.out.writeObject(new Message(attemptedUser, -2));
						}
					}catch(Exception e){
						System.err.println("VALIDATION ERROR");
					}
				}
			}
		}
		public void handleLookingForGame(Message message){
			System.err.println(name + " is looking for a game");
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
					System.out.println(name + " found a game against " + curr.getPlayerOne().name);
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
				System.out.println(name + " didn't find a game so they made one themselves");
				games.add(new ClientGames(this));
			}
		}
		public void handleMove(Message message){
			System.err.println(name + " TRYING TO MOVE: " + message.code);
			ClientGames remove = null;
			int code = 0;
			for(ClientGames game : games){
				if(game.getPlayerOne().name.equals(name)){
					System.out.println(name + " is attempting to drop a coin in column " + message.code);
					code = game.currentGame.makeMove(message.code, 1);
					try{
						if(code == 2){
							remove = game;
//							games.remove(game);
							wins++;
							game.getPlayerTwo().losses++;
							this.out.writeObject(new Message("WINNER"));
							game.getPlayerTwo().out.writeObject(new Message("LOSER"));
							System.out.println(name + " has won the game!");
						}else{
							this.out.writeObject(new Message(code, name, message.code));
							game.getPlayerTwo().out.writeObject(new Message(code, name, message.code));
						}

					}catch(Exception e){
						System.err.println("Error sending move validation");
					}
				}else if(game.getPlayerTwo().name.equals(name)){
					System.out.println(name + " is attempting to drop a coin in column " + message.code);
					code = game.currentGame.makeMove(message.code, 2);
					try{
						if(code == 2){
							remove = game;
							wins++;
//							game.getPlayerTwo().losses++;
							games.remove(game);
							this.out.writeObject(new Message("WINNER"));
							game.getPlayerTwo().out.writeObject(new Message("LOSER"));
							System.out.println(name + " has won the game!");
						}else{
							this.out.writeObject(new Message(code, name, message.code));
							game.getPlayerOne().out.writeObject(new Message(code, name, message.code));
						}

					}catch(Exception e){
						System.err.println("Error sending move validation");
					}
				}
			}
//			if(code == 2){
//				games.remove(remove);
//			}
		}
		public void handleChangeName(Message message){
			String fullFile = "";
			try{
				File f = new File("src/main/java/Users.txt");
				Scanner myReader = new Scanner(f);

//				loops through Users.txt to find for same username
				while(myReader.hasNextLine()){
					String line = myReader.nextLine();
					String[] parts = line.split(" ");

					if(parts[0].equals(name)) {
						//saves the users win and losses
						fullFile = fullFile.concat(message.message + " " + parts[1] + "\n");
					}else{
						fullFile = fullFile.concat(line + "\n");
					}
				}
			}catch(FileNotFoundException e){
				System.err.println("USER FILES NOT FOUND REALLY BIG ISSUE");
			}
			try{
				FileWriter myWriter = new FileWriter("src/main/java/Users.txt", false);
				myWriter.write(fullFile);
				myWriter.close();
			}catch(IOException e){
				System.err.println("COULDN'T OPEN USERS FILE");
			}
			try{
				System.out.println(name + " changed their name to " + message.message);
				name = message.message;
				this.out.writeObject(new Message(name, "", 0));
			}catch(Exception e){
				System.err.println("COULDN'T UPDATE NAME");
			}
		}
		public void handleRematch(Message message){
			if(message.code == 0){
				System.out.println(name + " rejected to rematch");
				ClientGames delete = null;
				for(ClientGames game : games){
					if(game.getPlayerOne().name.equals(name) || game.getPlayerTwo().name.equals(name)){
						if(game.getPlayerOne().name.equals(name)){
							try{
								game.getPlayerTwo().out.writeObject(new Message(0, "REMATCH"));
							}catch(Exception e){
								System.err.println("ERROR IN DECLINING REMATCH");
							}
						}else if(game.getPlayerTwo().name.equals(name)){
							try{
								game.getPlayerOne().out.writeObject(new Message(0, "REMATCH"));
							}catch(Exception e){
								System.err.println("ERROR IN DECLINING REMATCH");
							}
						}
						delete = game;
					}
				}
				games.remove(delete);
			}
			if(message.code == 1){
				System.out.println(name + " accepted the rematch");
				for(ClientGames game : games){
					if(game.getPlayerOne().name.equals(name) || game.getPlayerTwo().name.equals(name)){
						game.currentGame.clearGame();
						if(game.getPlayerOne().name.equals(name)){
							try{
								game.getPlayerTwo().out.writeObject(new Message(1, "REMATCH"));
							}catch(Exception e){
								System.err.println("ERROR IN REMATCH");
							}
						}else if(game.getPlayerTwo().name.equals(name)){
							try{
								game.getPlayerOne().out.writeObject(new Message(1, "REMATCH"));
							}catch(Exception e){
								System.err.println("ERROR IN REMATCH");
							}
						}
					}
				}
			}
		}

		public int[] getWinLoss(String attemptedUser){
			int[] winLoss = new int[2];
			boolean found = false;
			try{
				File f = new File("src/main/java/WL.txt");
				Scanner myReader = new Scanner(f);

//				loops through Users.txt to find for same username
				while(myReader.hasNextLine()){
					String line = myReader.nextLine();
					String[] parts = line.split(" ");

					if(parts[0].equals(attemptedUser)) {
						//saves the users win and losses
						found = true;
						winLoss[0] = Integer.parseInt(parts[1]);
						winLoss[1] = Integer.parseInt(parts[2]);

					}
				}

				if(!found){
					try{
						FileWriter myWriter = new FileWriter("src/main/java/WL.txt", true);
						myWriter.write(attemptedUser + " 0 0\n");
						myWriter.close();
					}catch(IOException e){
						System.err.println("COULDN'T OPEN USERS FILE");
					}
				}

			}catch(FileNotFoundException e){
				System.err.println("USER FILES NOT FOUND REALLY BIG ISSUE");
			}

			return winLoss;
		}

		public void updateWinLoss(){
			String fullFile = "";
			try{
				File f = new File("src/main/java/WL.txt");
				Scanner myReader = new Scanner(f);

//				loops through Users.txt to find for same username
				while(myReader.hasNextLine()){
					String line = myReader.nextLine();
					String[] parts = line.split(" ");

					if(parts[0].equals(name)) {
						//saves the users win and losses
						fullFile = fullFile.concat(name + " " + losses + " " + wins + "\n");
					}else{
						fullFile = fullFile.concat(line + "\n");
					}
				}
			}catch(FileNotFoundException e){
				System.err.println("USER FILES NOT FOUND REALLY BIG ISSUE");
			}
			try{
				FileWriter myWriter = new FileWriter("src/main/java/WL.txt", false);
				myWriter.write(fullFile);
				myWriter.close();
			}catch(IOException e){
				System.err.println("COULDN'T OPEN USERS FILE");
			}
//			System.out.println(fullFile);
		}

	}//end of client thread

}


	
	

	
