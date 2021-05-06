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

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;

import com.google.gson.Gson;

public class Main extends Application {
    private int stagesUnlocked = 1;
    private BreakoutRoom currentRoom = null;
    private Stage primaryStage;
    private Scene mainMenu;
    private ArrayList<Button> roomButtons;
    private BreakoutRoomSettings[] roomList;

    public void setUnlocked(int stage) {
        stagesUnlocked = Math.max(stagesUnlocked, stage);
        for (int i = 1; i <= roomButtons.size(); i++) {
            Button roomButton = roomButtons.get(i - 1);
            if (i <= stagesUnlocked) {
                roomButton.setText("Room " + i);
                roomButton.setTextFill(Color.WHITE);
                roomButton.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");
            }
            else {
                roomButton.setText("Locked");
                roomButton.setTextFill(Color.valueOf("#808080"));
                roomButton.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: #808080; -fx-border-width: 4;");
            }
            final int roomNumber = i;
            roomButton.setOnMouseClicked(event -> {
                playRoom(roomNumber);
            });
        }
    }

    public void playRoom(int roomNumber) {
        if (roomNumber > 0 && roomNumber <= roomList.length && roomNumber <= stagesUnlocked) {
            if (currentRoom != null) {
                currentRoom.stop();
            }

            currentRoom = new BreakoutRoom(this, this.roomList[roomNumber - 1],
            () -> {
                currentRoom = null;
                Platform.runLater(() ->{
                    primaryStage.setScene(mainMenu);
                });
            },
            () -> {
                setUnlocked(roomNumber + 1);
                playRoom(roomNumber + 1);
            });
            currentRoom.play(primaryStage);
        }
        else {
            currentRoom = null;
            primaryStage.setScene(mainMenu);
        }
    }

    public BreakoutRoomSettings[] loadRooms() throws Exception {
        Gson gson = new Gson();

        BufferedReader roomSettingsReader = new BufferedReader(new FileReader("Rooms.json"));
        return gson.fromJson(roomSettingsReader, BreakoutRoomSettings[].class);
    }

    public int loadUnlockedStages() {
        int unlocked = 1;
        try {
            BufferedReader savedDataRead = new BufferedReader(new FileReader("UnlockedStages.dat"));

            try {
                unlocked = Integer.parseInt(savedDataRead.readLine());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                savedDataWrite.write(Integer.toString(unlocked));
                savedDataWrite.flush();
                savedDataWrite.close();
            }
        }
        catch (FileNotFoundException e) {
            try {
                BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                savedDataWrite.write(Integer.toString(unlocked));
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
        return unlocked;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load Room Settings
        try {
            this.roomList = loadRooms();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        // Read saved data
        this.stagesUnlocked = loadUnlockedStages();

        // Create Main Menu
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

        // Room selection buttons
        GridPane roomSelection = new GridPane();
        roomSelection.setPrefSize(mainMenu.getWidth() * 2 / 3, mainMenu.getHeight() / 3);
        roomSelection.setLayoutX(mainMenu.getWidth() / 6);
        roomSelection.setLayoutY(mainMenu.getHeight() / 2.5);
        roomSelection.setAlignment(Pos.CENTER);
        roomSelection.setHgap(10);
        roomSelection.setVgap(10);

        this.roomButtons = new ArrayList<>();

        for (int roomNumber = 1; roomNumber <= this.loadRooms().length; roomNumber++) {
            int x = (roomNumber - 1) % 3;
            int y = (roomNumber - 1) / 3;

            Button roomButton = new Button("Room " + roomNumber);
            roomButton.setPrefSize(roomSelection.getPrefWidth() / 3, roomSelection.getPrefWidth() / 2);

            roomButton.setFont(new Font("Arial", 20));
            roomButton.setTextFill(Color.WHITE);
            roomButton.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");

            if (stagesUnlocked < roomNumber) {
                roomButton.setText("Locked");
                roomButton.setTextFill(Color.valueOf("#808080"));
                roomButton.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: #808080; -fx-border-width: 4;");
            }

            final int roomIndex = roomNumber;
            roomButton.setOnMouseClicked(event -> {
                playRoom(roomIndex);
            });
            roomSelection.add(roomButton, x, y);
            roomButtons.add(roomButton);
        }

        mainMenuPane.getChildren().add(roomSelection);

        // Cheat button
        Button unlockAll = new Button("Unlock All Rooms");
        unlockAll.setPrefSize(mainMenu.getWidth() / 5, mainMenu.getHeight() / 20);
        unlockAll.setLayoutX(mainMenu.getWidth() / 2 - mainMenu.getWidth() / 10);
        unlockAll.setLayoutY(mainMenu.getHeight() * .8);
        unlockAll.setTextFill(Color.WHITE);
        unlockAll.setFont(new Font("Arial", 20));
        unlockAll.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");
        unlockAll.setOnMouseClicked(event -> {
            setUnlocked(roomList.length);
            mainMenuPane.getChildren().remove(unlockAll);
        });
        mainMenuPane.getChildren().add(unlockAll);

        // Exit button
        Button exitBtn = new Button("Quit Game");
        exitBtn.setPrefSize(mainMenu.getWidth() / 5, mainMenu.getHeight() / 20);
        exitBtn.setLayoutX(mainMenu.getWidth() / 2 - mainMenu.getWidth() / 10);
        exitBtn.setLayoutY(mainMenu.getHeight() * .875);
        exitBtn.setTextFill(Color.WHITE);
        exitBtn.setFont(new Font("Arial", 20));
        exitBtn.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");
        exitBtn.setOnMouseClicked(event -> {
            if (currentRoom != null) {
                currentRoom.stop();
                currentRoom = null;
            }
            System.exit(0);
        });
        mainMenuPane.getChildren().add(exitBtn);

        // Stop application on window closed
        primaryStage.setOnCloseRequest(event -> {
            if (currentRoom != null) {
                currentRoom.stop();
                currentRoom = null;
            }
            System.exit(0);
        });

        // Show app window
        primaryStage.setResizable(false);
        primaryStage.setTitle("BreakoutFX");
        primaryStage.setScene(mainMenu);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
