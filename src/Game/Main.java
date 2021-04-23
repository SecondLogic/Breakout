/*
    Amos Cabudol
    CSPC-24500, Lewis University
    03/26/2021

    Breakout
    Game/Main.java
 */

package Game;

import Simulation.Vector2;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.layout.Pane;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Circle;

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

        // Create nodes
        Rectangle platform = new Rectangle();
        platform.setWidth(100);
        platform.setHeight(10);
        platform.setLayoutX((scene.getWidth() - platform.getWidth()) / 2);
        platform.setLayoutY(scene.getHeight() - platform.getHeight());
        platform.setFill(Color.WHITE);

        Circle ball = new Circle();
        ball.setRadius(8);
        ball.setCenterX(scene.getWidth() / 2);
        ball.setCenterY(scene.getHeight() - platform.getHeight() - ball.getRadius());
        ball.setFill(Color.WHITE);

        // Game Runtime Loop
        Task gameLoop = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Initialize states
                long lastTick = System.currentTimeMillis();
                double[] platformPos = {platform.getLayoutX(), platform.getLayoutY()};
                double[] ballPos = {ball.getCenterX(), ball.getCenterY()};
                double[] ballVelocity = {1000, -1000};

                while (!this.isCancelled()) {
                    // Get previous frame time
                    long currentTick = System.currentTimeMillis();
                    double deltaTime = (double) (currentTick - lastTick) / 1000;

                    // Run simulation if not paused
                    if (!isPaused) {

                        // Move Platform
                        Vector2 mouseLocation = inputListener.getMouseLocation();
                        platformPos[0] = Math.max(0, Math.min(scene.getWidth() - platform.getWidth(), mouseLocation.x - platform.getWidth() / 2));
                        /*
                        if (inputListener.isPressed(KeyCode.A) || inputListener.isPressed(KeyCode.LEFT)) {
                            platformPos[0] = Math.max(0, platformPos[0] - deltaTime * PLATFORM_SPEED);
                        }
                        else if (inputListener.isPressed(KeyCode.D) || inputListener.isPressed(KeyCode.RIGHT)) {
                            platformPos[0] = Math.min(scene.getWidth() - platform.getWidth(), platformPos[0] + deltaTime * PLATFORM_SPEED);
                        }
                        */

                        // Move Ball
                        double ballVelX = ballVelocity[0] * deltaTime;
                        if (ballVelX < 0) {
                            double overShoot = (ballPos[0] + ballVelX) + ball.getRadius();
                            if (overShoot < 0) {
                                ballPos[0] = ball.getRadius() - overShoot;
                                ballVelocity[0] = -ballVelocity[0];
                            } else {
                                ballPos[0] += ballVelX;
                            }
                        } else {
                            double overShoot = (ballPos[0] + ballVelX) - (scene.getWidth() - ball.getRadius());
                            if (overShoot > 0) {
                                ballPos[0] = scene.getWidth() - ball.getRadius() - overShoot;
                                ballVelocity[0] = -ballVelocity[0];
                            } else {
                                ballPos[0] += ballVelX;
                            }
                        }

                        double ballVelY = ballVelocity[1] * deltaTime;
                        if (ballVelY < 0) {
                            double overShoot = (ballPos[1] + ballVelY) + ball.getRadius();
                            if (overShoot < 0) {
                                ballPos[1] = ball.getRadius() - overShoot;
                                ballVelocity[1] = -ballVelocity[1];
                            } else {
                                ballPos[1] += ballVelY;
                            }
                        } else {
                            double overShoot = (ballPos[1] + ballVelY) - (scene.getHeight() - ball.getRadius());
                            if (overShoot > 0) {
                                ballPos[1] = scene.getHeight() - ball.getRadius() - overShoot;
                                ballVelocity[1] = -ballVelocity[1];
                            } else {
                                ballPos[1] += ballVelY;
                            }
                        }

                        // Redraw scene
                        platform.setLayoutX(platformPos[0]);
                        platform.setLayoutY(platformPos[1]);
                        Thread.sleep(0);
                        ball.setCenterX(ballPos[0]);
                        ball.setCenterY(ballPos[1]);
                    }

                    // End frame
                    lastTick = currentTick;
                    Thread.sleep(16);  // ~60hz
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
            setPaused(false);
        });

        // Add nodes to main pane
        mainPane.getChildren().addAll(platform, ball);

        // Show Scene
        primaryStage.setTitle("Breakout");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Run game loop
        Thread runGameLoop = new Thread(gameLoop);
        runGameLoop.start();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
