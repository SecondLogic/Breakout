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
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
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
        Vector2 paddleSize = new Vector2(100,10);
        Vector2 paddlePos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y / 2);
        SimulatedRectangle paddle = new SimulatedRectangle(paddleSize, paddlePos, simulationSpace);

        // Ball
        Vector2 ballPos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y - 8);
        SimulatedCircle ball = new SimulatedCircle(8, ballPos, simulationSpace);
        ball.setAnchored(false);

        // Bricks
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 5; y++) {
                Vector2 brickSize = new Vector2(50,20);
                Vector2 brickPos = new Vector2(30 + 55 * x, 15 + 25 * y);
                //Vector2 brickPos = new Vector2(Math.random() * scene.getWidth(),Math.random() * scene.getHeight());
                SimulatedRectangle brick = new SimulatedRectangle(brickSize, brickPos, simulationSpace);
            }
        }

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

                                ball.moveTo(mouseLocation);

                                // Run simulation
                                simulationSpace.simulate();
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
