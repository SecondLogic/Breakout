/*
    Amos Cabudol
    CSPC-24500, Lewis University
    03/26/2021

    Breakout
    Game/Main.java

    Sources:
    Debanth, Multithreading in JavaFX
        https://www.developer.com/design/multithreading-in-javafx/
 */

package Game;

import Simulation.SimulatedRectangle;
import Simulation.SimulatedCircle;
import Simulation.SimulationSpace;
import Structures.Vector2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main extends Application {

    // Game paused status
    public static boolean isPaused = false;
    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        // Settings
        //final double PLATFORM_SPEED = 1500;  // Pixels per second

        boolean paused = false;

        // Create Scene
        Pane mainPane = new Pane();
        mainPane.setStyle("-fx-background-color: #000000;");
        Scene scene = new Scene(mainPane, 1280, 720);

        // Initialize input listener
        InputListener inputListener = new InputListener(scene);

        // Create Simulation Space
        SimulationSpace simulationSpace = new SimulationSpace(scene);

        // Create objects

        // Paddle
        Vector2 paddleSize = new Vector2(120,12);
        Vector2 paddlePos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y / 2);
        SimulatedRectangle paddle = new SimulatedRectangle(paddleSize, paddlePos, simulationSpace);

        // Ball
        Vector2 ballSize = new Vector2(16,16);
        Vector2 ballPos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y - 12);
        SimulatedRectangle ball = new SimulatedRectangle(ballSize, ballPos, simulationSpace);

        //Vector2 ballPos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y - 12);
        //SimulatedCircle ball = new SimulatedCircle(8, ballPos, simulationSpace);
        ball.setOnCollide(collision -> {
            if (collision.collidedShape == paddle) {
                ball.setColor(Color.RED);
            }
            ball.setVelocity(Vector2.ZERO);
        });
        ball.setVelocity(new Vector2(1, -1).unit().product(500));
        ball.setAnchored(false);

        // Walls
        Vector2 wallTopSize = new Vector2(scene.getWidth(), 100);
        Vector2 wallTopPos = new Vector2(scene.getWidth() / 2, -wallTopSize.y / 2);
        SimulatedRectangle wallTop = new SimulatedRectangle(wallTopSize, wallTopPos, simulationSpace);

        Vector2 wallLeftSize = new Vector2(100, scene.getHeight());
        Vector2 wallLeftPos = new Vector2(-wallLeftSize.x / 2, scene.getHeight() / 2);
        SimulatedRectangle wallLeft = new SimulatedRectangle(wallLeftSize, wallLeftPos, simulationSpace);

        Vector2 wallRightSize = new Vector2(100, scene.getHeight());
        Vector2 wallRightPos = new Vector2(scene.getWidth() + wallRightSize.x / 2, scene.getHeight() / 2);
        SimulatedRectangle wallRight = new SimulatedRectangle(wallRightSize, wallRightPos, simulationSpace);

        // Bricks
        Vector2 brickSize = new Vector2(60,24);
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 5; y++) {
                Vector2 brickPos = new Vector2((brickSize.x + 4) * (x + 0.5), (brickSize.y + 4) * (y + 0.5));
                SimulatedRectangle brick = new SimulatedRectangle(brickSize, brickPos, simulationSpace);

                brick.setOnCollide(collision -> {
                    if (collision.collidedShape == ball) {
                        Platform.runLater(() -> {
                            simulationSpace.remove(brick);
                        });
                    }
                });
            }
        }

        // Debug Text
        Text cursorPos = new Text();
        cursorPos.setLayoutX(4);
        cursorPos.setLayoutY(scene.getHeight() - 48);
        cursorPos.setText("Cursor: ");
        cursorPos.setFont(Font.font("Courier New", 14));
        cursorPos.setFill(Color.WHITE);
        mainPane.getChildren().add(cursorPos);

        // Game Runtime Loop
        Task gameLoop = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    while (!this.isCancelled()) {
                        // Run simulation if not paused
                        if (!isPaused) {
                            if (!simulationSpace.isSimulating()) {

                                // Move Paddle
                                Vector2 mouseLocation = inputListener.getMouseLocation();
                                paddle.moveTo(new Vector2(Math.max(paddle.getSize().x / 2, Math.min(scene.getWidth() - paddle.getSize().x / 2, mouseLocation.x)), paddle.getPosition().y));

                                // Run simulation
                                simulationSpace.simulate();

                                // Debug Text
                                Platform.runLater(() -> {
                                    cursorPos.setText("Cursor: " + mouseLocation);
                                });
                            }
                        }
                        Thread.sleep(16);  // ~60hz
                    }
                }
                catch (Exception e) {
                    // Display any errors that occur during runtime
                    e.printStackTrace();
                }

                return null;
            }
        };

        gameLoop.setOnCancelled(event -> {
            try {
                stop();
            }
            catch (Exception e) {
                System.out.println(e);
            }
        });

        primaryStage.setOnCloseRequest(event -> {
            if (gameLoop.isRunning()) {
                gameLoop.cancel();
            }
        });

        // Pause on cursor off-screen
        scene.setOnMouseExited(input -> {
            setPaused(true);
        });

        scene.setOnMouseEntered(input -> {
            simulationSpace.resetTick();
            setPaused(false);
        });

        // Show Scene
        primaryStage.setTitle("Breakout");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Run game loop
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(gameLoop);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
