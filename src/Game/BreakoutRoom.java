/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/05/2021

    Breakout
    Game/BreakoutRoom.java

    Sources:
    Debanth, Multithreading in JavaFX
        https://www.developer.com/design/multithreading-in-javafx/
 */


package Game;

import Simulation.SimulatedRectangle;
import Simulation.SimulationSpace;
import Structures.Vector2;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BreakoutRoom {
    private final ArrayList<BreakoutBall> balls;
    private final ArrayList<BreakoutBrick> bricks;
    private final BreakoutPaddle paddle;
    private final BreakoutRoomSettings settings;
    private final SimulationSpace simulationSpace;
    private final Task gameLoop;
    private final Scene scene;
    private final Runnable onExit;
    private final Runnable onAdvance;
    private int turns;
    private double waitDuration;
    private boolean gamePaused;
    private boolean gameStopped;
    private boolean gameWaiting;
    private boolean cleared;
    private long waitStart;
    private Text turnsDisplay;
    private Text gameMsgDisplay;

    public BreakoutRoom(Main game, BreakoutRoomSettings settings, Runnable onExit, Runnable onAdvance) {
        this.onExit = onExit;
        this.onAdvance = onAdvance;
        this.balls = new ArrayList<>();
        this.bricks = new ArrayList<>();
        this.settings = settings;
        this.gamePaused = false;
        this.gameStopped = false;
        this.gameWaiting = false;
        this.cleared = false;
        this.waitDuration = 0;
        this.turns = this.settings.startingTurns;

        // Create Scene
        Pane mainPane = new Pane();
        mainPane.setStyle("-fx-background-color: #000000;");
        this.scene = new Scene(mainPane, this.settings.sceneDimensions.x, this.settings.sceneDimensions.y);

        // Initialize input listener
        InputListener inputListener = new InputListener(this.scene);

        // Create Simulation Space
        this.simulationSpace = new SimulationSpace(this.scene);

        // Create objects

        // Game Text
        this.gameMsgDisplay = new Text();
        this.gameMsgDisplay.setLayoutX(this.scene.getWidth() / 2);
        this.gameMsgDisplay.setLayoutY(this.scene.getHeight() / 2);
        this.gameMsgDisplay.setFont(new Font("Arial Bold", this.scene.getHeight() / 8));
        this.gameMsgDisplay.setTextAlignment(TextAlignment.CENTER);
        this.gameMsgDisplay.setFill(Color.WHITE);


        this.turnsDisplay = new Text();
        this.turnsDisplay.setLayoutX(8);
        this.turnsDisplay.setLayoutY(scene.getHeight() * .9);
        this.turnsDisplay.setFont(new Font("Arial", this.scene.getHeight() / 36));
        this.turnsDisplay.setFill(Color.WHITE);
        this.turnsDisplay.setText("Turns Left: " + this.turns + "\n[P] Pause\n[X] Main Menu");

        // Walls (off-screen)
        Vector2 wallTopSize = new Vector2(scene.getWidth(), 100);
        Vector2 wallTopPos = new Vector2(scene.getWidth() / 2, -wallTopSize.y / 2);
        SimulatedRectangle wallTop = new SimulatedRectangle(wallTopSize, wallTopPos, this.simulationSpace);

        Vector2 wallLeftSize = new Vector2(100, scene.getHeight());
        Vector2 wallLeftPos = new Vector2(-wallLeftSize.x / 2, scene.getHeight() / 2);
        SimulatedRectangle wallLeft = new SimulatedRectangle(wallLeftSize, wallLeftPos, this.simulationSpace);

        Vector2 wallRightSize = new Vector2(100, scene.getHeight());
        Vector2 wallRightPos = new Vector2(this.scene.getWidth() + wallRightSize.x / 2, this.scene.getHeight() / 2);
        SimulatedRectangle wallRight = new SimulatedRectangle(wallRightSize, wallRightPos, this.simulationSpace);

        // Bricks
        Vector2 brickSize = new Vector2(60,24);
        for (BreakoutBrickSettings brickData : this.settings.getBrickData()) {
            new BreakoutBrick(brickData, this);
        }

        // Paddle
        this.paddle = new BreakoutPaddle(new Vector2(this.settings.getPaddleWidth(), 24), new Vector2(scene.getWidth() / 2, scene.getHeight()), this);
        this.simulationSpace.add(this.paddle);

        // Ball
        Vector2 defaultBallPos = new Vector2(scene.getWidth() / 2, this.paddle.getPosition().y - this.paddle.getSize().y / 2 - this.settings.getBallRadius() - 2);
        new BreakoutBall(this.settings.getBallRadius(), defaultBallPos, this);

        this.pauseWait(3);

        Platform.runLater(() -> {
            mainPane.getChildren().add(this.gameMsgDisplay);
            mainPane.getChildren().add(this.turnsDisplay);
        });

        // Game Runtime Loop
        this.gameLoop = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    while (!this.isCancelled()) {
                        if (inputListener.consumePress(KeyCode.X)) {
                            if (cleared) {
                                game.setUnlocked(settings.roomNumber + 1);
                            }
                            onExit.run();
                            this.cancel();
                        }
                        if (!gameStopped) {
                            if (inputListener.consumePress(KeyCode.P)) {
                                setPaused(!gamePaused);
                            }

                            // Run simulation if not paused
                            if (!gamePaused) {
                                waitDuration -= (double) (System.currentTimeMillis() - waitStart) / 1000;
                                waitStart = System.currentTimeMillis();
                                if (waitDuration <= 0) {
                                    gameWaiting = false;
                                    Platform.runLater(() -> {
                                        gameMsgDisplay.setText("");
                                    });
                                } else {
                                    gameWaiting = true;
                                    simulationSpace.resetTick();
                                    Platform.runLater(() -> {
                                        gameMsgDisplay.setText(Integer.toString((int) Math.ceil(waitDuration)));
                                        gameMsgDisplay.setLayoutX(scene.getWidth() / 2 - gameMsgDisplay.getLayoutBounds().getWidth() / 2);
                                    });
                                }

                                if (!gameWaiting) {
                                    if (!simulationSpace.isSimulating()) {
                                        // Move Paddle
                                        Vector2 mouseLocation = inputListener.getMouseLocation();
                                        paddle.moveTo(new Vector2(Math.max(paddle.getSize().x / 2, Math.min(scene.getWidth() - paddle.getSize().x / 2, mouseLocation.x)), paddle.getPosition().y));

                                        // Run simulation
                                        simulationSpace.simulate();
                                    }
                                }
                            } else {
                                simulationSpace.resetTick();
                                waitStart = System.currentTimeMillis();
                                gameMsgDisplay.setText("Paused");
                                gameMsgDisplay.setLayoutX(scene.getWidth() / 2 - gameMsgDisplay.getLayoutBounds().getWidth() / 2);
                            }
                        }
                        Thread.sleep(16);  // ~60hz
                    }
                }
                catch (Exception e) {
                    // Display any errors that occur during runtime
                    if (!(e instanceof InterruptedException)) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };
    }

    public void setPaused(boolean paused) {
        this.gamePaused = paused;
    }

    public BreakoutRoomSettings getSettings() { return this.settings; }

    public void play(Stage stage){
        // Show room
        stage.setScene(this.scene);

        // Run game loop
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this.gameLoop);
    }

    public void pauseWait(double duration) {
        this.waitStart = System.currentTimeMillis();
        this.waitDuration = duration;
    }

    public void showExitButton() {
        Button exitBtn = new Button("Main Menu");
        exitBtn.setPrefSize(this.scene.getWidth() / 5, this.scene.getHeight() / 20);
        exitBtn.setLayoutX(this.scene.getWidth() / 2 - this.scene.getWidth() / 10);
        exitBtn.setLayoutY(this.scene.getHeight() * .875);
        exitBtn.setTextFill(Color.WHITE);
        exitBtn.setFont(new Font("Arial", 20));
        exitBtn.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");
        exitBtn.setOnMouseClicked(event -> {
            this.gameLoop.cancel();
            this.onExit.run();
        });
        Platform.runLater(() -> {
            ((Pane) this.scene.getRoot()).getChildren().add(exitBtn);
        });
    }

    public void showAdvanceButton() {
        Button advanceBtn = new Button("Next Room");
        advanceBtn.setPrefSize(this.scene.getWidth() / 5, this.scene.getHeight() / 20);
        advanceBtn.setLayoutX(this.scene.getWidth() / 2 - this.scene.getWidth() / 10);
        advanceBtn.setLayoutY(this.scene.getHeight() * .8);
        advanceBtn.setTextFill(Color.WHITE);
        advanceBtn.setFont(new Font("Arial", 20));
        advanceBtn.setStyle("-fx-background-color: black; -fx-smooth: false; -fx-border-color: white; -fx-border-width: 4;");
        advanceBtn.setOnMouseClicked(event -> {
            this.gameLoop.cancel();
            this.onAdvance.run();
        });
        Platform.runLater(() -> {
            ((Pane) this.scene.getRoot()).getChildren().add(advanceBtn);
        });
    }

    public void add(BreakoutBrick brick) {
        if (!this.bricks.contains(brick)) {
            brick.updateColor();
            this.bricks.add(brick);
            this.simulationSpace.add(brick);
        }
    }

    public void add(BreakoutBall ball) {
        if (!this.balls.contains(ball)) {
            this.balls.add(ball);
            this.simulationSpace.add(ball);
        }
    }

    public void stop() {
        if (this.gameLoop.isRunning()) {
            onExit.run();
            this.gameLoop.cancel();
        }
    }

    public void remove(BreakoutBrick brick) {
        this.bricks.remove(brick);
        if (bricks.size() == 0) {
            this.gameStopped = true;
            this.cleared = true;

            // Increment unlocked stages
            try {
                BufferedReader savedDataRead = new BufferedReader(new FileReader("UnlockedStages.dat"));

                try {
                    int stagesUnlocked = Integer.parseInt(savedDataRead.readLine());
                    if (this.settings.roomNumber + 1 > stagesUnlocked) {
                        BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                        savedDataWrite.write(Integer.toString(this.settings.roomNumber + 1));
                        savedDataWrite.flush();
                        savedDataWrite.close();
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                    savedDataWrite.write(Integer.toString(this.settings.roomNumber + 1));
                    savedDataWrite.flush();
                    savedDataWrite.close();
                }
            }
            catch (FileNotFoundException e) {
                try {
                    BufferedWriter savedDataWrite = new BufferedWriter(new FileWriter("UnlockedStages.dat"));
                    savedDataWrite.write(Integer.toString(this.settings.roomNumber + 1));
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

            Platform.runLater(() -> {
                this.gameMsgDisplay.setText("Room Clear!");
                this.gameMsgDisplay.setLayoutX(this.scene.getWidth() / 2 - this.gameMsgDisplay.getLayoutBounds().getWidth() / 2);
            });
            showAdvanceButton();
            showExitButton();
        }
        Platform.runLater(() -> {
            this.simulationSpace.remove(brick);
        });
    }

    public void remove(BreakoutBall ball) {
        this.balls.remove(ball);
        if (balls.size() == 0) {
            turns -= 1;
            if (turns == 0) {
                this.gameStopped = true;
                Platform.runLater(() -> {
                    this.turnsDisplay.setText("Turns Left: " + this.turns + "\n[P] Pause\n[X] Main Menu");
                    this.gameMsgDisplay.setText("Game Over");
                    this.gameMsgDisplay.setLayoutX(scene.getWidth() / 2 - this.gameMsgDisplay.getLayoutBounds().getWidth() / 2);
                });
                showExitButton();
            }
            else {
                Vector2 defaultBallPos = new Vector2(scene.getWidth() / 2, this.paddle.getPosition().y - this.paddle.getSize().y / 2 - this.settings.getBallRadius() - 2);
                new BreakoutBall(this.settings.getBallRadius(), defaultBallPos, this);
                this.paddle.moveTo(new Vector2(this.scene.getWidth() / 2, this.scene.getHeight()));
                this.pauseWait(3);
                Platform.runLater(() -> {
                    this.turnsDisplay.setText("Turns Left: " + this.turns + "\n[P] Pause\n[X] Main Menu");
                });
            }
        }
        Platform.runLater(() -> {
            this.simulationSpace.remove(ball);
        });
    }
}
