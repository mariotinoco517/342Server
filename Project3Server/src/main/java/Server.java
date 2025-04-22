import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	int count = 1;	
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
//	ArrayList<ClientThread> loggedInClients = new ArrayList<>();
	HashMap<String, Integer> loggedInClient = new HashMap<>();
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
					for(ClientThread t: clients){
						//if code is to send to all or threads name matches send text
						if(message.recipient.equals("Send to All") || message.recipient.equals(t.name) ) {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								System.err.println("New User Error");
							}
						}
					}
					break;
				case NEWUSER:
					//Updates existing clients about new client connection
					for(ClientThread t : clients) {
						if(this != t) {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								System.err.println("New User Error");
							}
						}
					}
					break;
				case DISCONNECT:
					//tells remaining clients of a disconnected client
					for(ClientThread t : clients) {
						try {
							t.out.writeObject(message);
						} catch (Exception e) {
							System.err.println("New User Error");
						}
					}
					break;
				case USERS:
					//sends array of existing clients to new client
					for(ClientThread t : clients) {
						if(this == t) {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								System.err.println("New User Error");
							}
						}
					}
					break;
				case VALIDNAME:
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
//										updateClients(new Message(loggedInClient));
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
//							updateClients(new Message(loggedInClient));
						}catch(IOException e){
							System.err.println("COULDN'T OPEN USERS FILE");
						}
					}
					message.code = code;
					for(ClientThread t : clients) {
						if(this == t) {
							try{
								t.out.writeObject(message);
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
					break;

				case PLAYERMOVE:
					//todo implementation of game logic
					break;
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

	}//end of client thread
}


	
	

	
