/*
    Amos Cabudol
    CSPC-24500, Lewis University
    03/26/2021

    Breakout
    Game/Main.java
 */

package Game;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class Main extends Application {
    private int stagesUnlocked = 1;
    private BreakoutRoom currentRoom = null;
    private Stage primaryStage;
    private Scene mainMenu;
    private ArrayList<Button> roomButtons;

    public void setUnlocked(int stage) {
        stagesUnlocked = Math.max(stagesUnlocked, stage);
        for (int i = 1; i <= roomButtons.size(); i++) {
            Button roomButton = roomButtons.get(i - 1);
            if (i <= stagesUnlocked) {
                roomButton.setText("Room " + i);
            }
            else {
                roomButton.setText("Locked");
            }
            final int roomNumber = i;
            roomButton.setOnMouseClicked(event -> {
                playRoom(roomNumber);
            });
        }
    }

    public void playRoom(int roomNumber) {
        if (roomNumber <= stagesUnlocked) {
            if (currentRoom != null) {
                currentRoom.stop();
            }

            currentRoom = new BreakoutRoom(this, new BreakoutRoomSettings(), () -> {
                currentRoom = null;
                Platform.runLater(() ->{
                    primaryStage.setScene(mainMenu);
                });
            });
            currentRoom.play(primaryStage);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        Pane mainMenuPane = new Pane();
        mainMenuPane.setStyle("-fx-background-color: #000000;");
        this.mainMenu = new Scene(mainMenuPane, 1280, 720);

        // Title
        Text title = new Text();
        title.setFont(new Font("Arial Bold", mainMenu.getHeight() / 6));
        title.setFill(Color.WHITE);
        title.setText("BreakoutFX");
        title.setLayoutX(mainMenu.getWidth() * 0.5 - title.getLayoutBounds().getWidth() / 2);
        title.setLayoutY(mainMenu.getHeight() * .333 - title.getLayoutBounds().getHeight() / 2);
        mainMenuPane.getChildren().add(title);

        // Read saved data
        try {
            BufferedReader savedDataRead = new BufferedReader(new FileReader("UnlockedStages.dat"));

            try {
                stagesUnlocked = Integer.parseInt(savedDataRead.readLine());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                savedDataWrite.write(Integer.toString(stagesUnlocked));
                savedDataWrite.flush();
                savedDataWrite.close();
            }
        }
        catch (FileNotFoundException e) {
            try {
                BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                savedDataWrite.write(Integer.toString(stagesUnlocked));
                savedDataWrite.flush();
                savedDataWrite.close();
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // Room selection buttons
        GridPane roomSelection = new GridPane();
        roomSelection.setPrefSize(mainMenu.getWidth() * 2 / 3, mainMenu.getHeight() / 3);
        roomSelection.setLayoutX(mainMenu.getWidth() / 6);
        roomSelection.setLayoutY(mainMenu.getHeight() / 2);
        roomSelection.setAlignment(Pos.CENTER);
        roomSelection.setHgap(10);
        roomSelection.setVgap(10);

        this.roomButtons = new ArrayList<>();

        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 3; x++) {
                int roomNumber = y * 3 + x + 1;
                Button roomButton = new Button("Room " + roomNumber);
                roomButton.setPrefSize(roomSelection.getPrefWidth() / 3, roomSelection.getPrefWidth() / 2);
                roomButton.setTextFill(Color.WHITE);
                roomButton.setFont(new Font("Arial", 20));
                roomButton.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");

                if (stagesUnlocked < roomNumber) {
                    roomButton.setText("Locked");
                }

                roomButton.setOnMouseClicked(event -> {
                    playRoom(roomNumber);
                });
                roomSelection.add(roomButton, x, y);
                roomButtons.add(roomButton);
            }
        }

        mainMenuPane.getChildren().add(roomSelection);

        // Stop application on window closed
        primaryStage.setOnCloseRequest(event -> {
            if (currentRoom != null) {
                currentRoom.stop();
                currentRoom = null;
            }
            System.exit(0);
        });

        // Show app window
        primaryStage.setScene(mainMenu);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
