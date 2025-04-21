import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
		
					ClientThread c = new ClientThread(mysocket.accept(), count);
					callback.accept(new Message(count,true));
					clients.add(c);
					c.start();
				
					count++;
				
			    }
			}//end of try
			catch(Exception e) {
				callback.accept(new Message("Server did not launch"));
			}
		}//end of while
	}
	

	class ClientThread extends Thread{
			
		
		Socket connection;
		int count;
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
						if(message.recipient==-1 || message.recipient==t.count ) {
							try {
								t.out.writeObject(message);
							} catch (Exception e) {
								System.err.println("New User Error");
							}
						}
					}
					break;
				case NEWUSER:
					ArrayList<Integer> toSend = new ArrayList<>();
					for(ClientThread t : clients) {
						if(this != t) {
							try {
								toSend.add(t.count);
								t.out.writeObject(message);
							} catch (Exception e) {
								System.err.println("New User Error");
							}
						}
					}
					callback.accept(new Message(toSend));

					break;

				case DISCONNECT:
					for(ClientThread t : clients) {
						try {
							t.out.writeObject(message);
						} catch (Exception e) {
							System.err.println("New User Error");
						}
					}
					break;
				case USERS:
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
								if(message.recipient == 0){
									code = 414;
									break;
								}
								String pass = line.substring(line.indexOf(" ") + 1);
								//client gave valid username and password combo when logging in
								if(pass.equals(attemptedPass)) {
									if(message.recipient == 1){
										code = 1;
									}

								}
								else{//client gave valid username but invalid password when logging in
									if(message.recipient == 1){
										code = -1;
									}
								}
								break;
							}
						}

					}catch(FileNotFoundException e){
						System.err.println("USER FILES NOT FOUND REALLY BIG ISSUE");
					}
					if(code == 404 && message.recipient == 0){
						try{
							FileWriter myWriter = new FileWriter("src/main/java/Users.txt", true);
							myWriter.write(attemptedUser + " " + attemptedPass + "\n");
							myWriter.close();
							code = 1;
						}catch(IOException e){
							System.err.println("COULDN'T OPEN USERS FILE");
						}
					}
					message.recipient = code;
					for(ClientThread t : clients) {
						if(this == t) {
							try{
								t.out.writeObject(message);
							}catch(Exception e){
								System.err.println("VALIDATION ERROR");
							}

						}
					}
					break;

				case PLAYERMOVE:
					//implementation of game logic
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

			updateClients(new Message(count,true));
			ArrayList<Integer> toSend = new ArrayList<>();
			for(ClientThread t : clients) {
				if(this != t) {
					toSend.add(t.count);
				}
			}
			updateClients(new Message(toSend));

			while(true) {
				try {
					Message data = (Message) in.readObject();
					callback.accept(data);
					updateClients(data);
				}
				catch(Exception e) {
					e.printStackTrace();
					Message discon = new Message(count, false);
					callback.accept(discon);
					updateClients(discon);
					clients.remove(this);
					break;
				}
			}
		}//end of run

	}//end of client thread
}


	
	

	
