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
import Simulation.SimulatedShape;
import Simulation.SimulationSpace;
import Structures.Vector2;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Main extends Application {
    // Settings
    public static double defaultBallSpeed = 500;    // How much speed the ball has when the game starts
    public static double minBallDeflection = 20;    // How much angle the ball has when it hits the center of the paddle
    public static double maxBallDeflection = 60;    // How much angle the ball has when it hits the end of the paddle
    public static double ballSpeedIncrement = 10;   // How much the ball gets faster when it hits something

    // Status vars
    public static double ballSpeed = defaultBallSpeed;
    public static SimulatedShape lastCollided = null;;
    public static void incrementBallSpeed(SimulatedShape collided) {
        if (collided != lastCollided) {
            ballSpeed += ballSpeedIncrement;
            lastCollided = collided;
        }
    }

    public static boolean isPaused = false;
    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    @Override
    public void start(Stage primaryStage) throws Exception{
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
        Vector2 defaultBallPos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y - 20);
        SimulatedRectangle ball = new SimulatedRectangle(ballSize, defaultBallPos, simulationSpace);

        //Vector2 ballPos = new Vector2(scene.getWidth() / 2, scene.getHeight() - paddleSize.y - 12);
        //SimulatedCircle ball = new SimulatedCircle(8, ballPos, simulationSpace);

        // Ball collision resolution
        ball.setOnCollide(collision -> {
            incrementBallSpeed(collision.collidedShape);
            if (collision.collidedShape == paddle) {
                double angleAlpha = (ball.getPosition().x - paddle.getPosition().x) / (paddle.getSize().x / 2);
                double angle = -90 + Math.sin(angleAlpha) * minBallDeflection + angleAlpha * (maxBallDeflection - minBallDeflection);
                ball.setVelocity(Vector2.rotationToVector(angle).product(ballSpeed));
            }
            else {
                ball.setVelocity(ball.getVelocity().reflect(collision.collisionAxis.left()));
            }
            ball.moveTo(ball.getPosition().sum(ball.getVelocity().product(collision.leftOverTime)));
        });
        ball.setVelocity(Vector2.rotationToVector(-45 + Math.random() * -90).product(ballSpeed));
        ball.setAnchored(false);

        // Walls (off-screen)
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
        ArrayList<SimulatedShape> bricks = new ArrayList<>();
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 5; y++) {
                Vector2 brickPos = new Vector2((brickSize.x + 4) * (x + 0.5), (brickSize.y + 4) * (y + 0.5));
                SimulatedRectangle brick = new SimulatedRectangle(brickSize, brickPos, simulationSpace);
                bricks.add(brick);
                brick.setOnCollide(collision -> {
                    bricks.remove(brick);
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
        cursorPos.setLayoutY(scene.getHeight() - 110);
        cursorPos.setText("Cursor: \nBall Pos: \nBall Speed:\nPress [R] to reset ball\nPress [M] to move ball to cursor\nPress [V] to make ball move towards cursor");
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

                                // Reset Ball if reached bottom
                                if (ball.getPosition().y > scene.getHeight()) {
                                    ballSpeed = defaultBallSpeed;
                                    ball.moveTo(defaultBallPos);
                                    ball.setVelocity(Vector2.rotationToVector(-45 + Math.random() * -90).product(ballSpeed));
                                }

                                // Run simulation
                                simulationSpace.simulate();

                                // Debug Text
                                Platform.runLater(() -> {
                                    cursorPos.setText(
                                            "Cursor: " + mouseLocation
                                        + "\nBall Pos: (" + Math.floor(ball.getPosition().x) + ", " + Math.floor(ball.getPosition().y)
                                        + ")\nBall Speed: " + ballSpeed
                                        + "\nPress [R] to reset ball\nPress [M] to move ball to cursor\nPress [V] to make ball move towards cursor");
                                });

                                // Reset Ball
                                if (inputListener.isPressed(KeyCode.R)) {
                                    ballSpeed = defaultBallSpeed;
                                    ball.moveTo(defaultBallPos);
                                    ball.setVelocity(Vector2.rotationToVector(-45 + Math.random() * -90).product(ballSpeed));
                                }
                                else if (inputListener.isPressed(KeyCode.M)) {
                                    ball.moveTo(mouseLocation);
                                }
                                else if (inputListener.isPressed(KeyCode.V)) {
                                    Vector2 dir = mouseLocation.sum(ball.getPosition().product(-1));
                                    if (dir == Vector2.ZERO) {
                                        dir = new Vector2(0,-1);
                                    }
                                    ball.setVelocity(dir.unit().product(ballSpeed));
                                    ball.moveTo(ball.getPosition().sum(ball.getVelocity().unit().product(.01)));
                                }
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
