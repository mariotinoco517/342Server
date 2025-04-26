
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.function.Consumer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiServer extends Application{

	Server serverConnection;
	
	ListView<String> listItems;
	ListView<String> listUsers;
	ArrayList<Integer> users = new ArrayList<>();

	HBox lists;
	
	
	public static void main(String[] args) {
		launch(args);
	}


	@Override
	public void start(Stage primaryStage) throws Exception {
		//starts the server connection
		serverConnection = new Server(data->{
			Platform.runLater(()->{
				//this is only to modify server GUI based on client request
				switch (data.type){
					case TEXT:
						listItems.getItems().add(data.recipient+": "+data.message);
						break;
					case NEWUSER:
						listUsers.getItems().add(String.valueOf(data.code));
						listItems.getItems().add(data.code + " has joined!");
						users.add(data.code);
						break;
					case DISCONNECT:
						listUsers.getItems().remove(String.valueOf(data.code));
						listItems.getItems().add(data.code + " has disconnected!");
//						users.remove(data.code);
						break;
					case LOOKINGFORGAME:
						listItems.getItems().add(data.message + " is looking for a game");
					//case PLAYERMOVE: still figuring out if this fits in here
				}
			});
		});

		
		listItems = new ListView<String>();
		listUsers = new ListView<String>();

		lists = new HBox(listUsers,listItems);


		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: coral");

		pane.setCenter(lists);
		pane.setStyle("-fx-font-family: 'serif'");


		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent t) {
                Platform.exit();
                System.exit(0);
            }
        });

		primaryStage.setScene(new Scene(pane, 500, 400));
		primaryStage.setTitle("This is the Server");
		primaryStage.show();
		
	}
}
