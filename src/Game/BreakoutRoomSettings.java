/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/05/2021

    Breakout
    Game/BreakoutSettings.java
 */

package Game;

import Structures.Vector2;

public class BreakoutRoomSettings {
    private double defaultBallSpeed;
    private double maxBallSpeed;
    private double minBallDeflection;
    private double maxBallDeflection;
    private double ballSpeedIncrement;
    private double ballRadius;
    private double paddleWidth;
    private final BreakoutBrickSettings[] bricks;
    public final Vector2 sceneDimensions;
    public final int startingTurns;
    public final int roomNumber;

    public BreakoutRoomSettings() {
        this.roomNumber = 1;

        this.defaultBallSpeed = 500;
        this.maxBallSpeed = 3000;
        this.minBallDeflection = 20;
        this.maxBallDeflection = 60;
        this.ballSpeedIncrement = 5;
        this.sceneDimensions = new Vector2(1280, 720);
        this.startingTurns = 99;

        this.ballRadius = 8;
        this.paddleWidth = 120;

        Vector2 brickSize = new Vector2(60, 24);
        this.bricks = new BreakoutBrickSettings[200];
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 5; y++) {
                this.bricks[x * 5 + y] = new BreakoutBrickSettings(
                    brickSize,
                    new Vector2((brickSize.x + 4) * (x + 0.5), (brickSize.y + 4) * (y + 0.5)),
                    0,
                    1
                );
            }
        }
    }

    public double getDefaultBallSpeed() { return this.defaultBallSpeed; }
    public double getMaxBallSpeed() { return this.maxBallSpeed; }
    public double getMinBallDeflection() { return this.minBallDeflection; }
    public double getMaxBallDeflection() { return this.maxBallDeflection; }
    public double getBallSpeedIncrement() { return this.ballSpeedIncrement; }
    public double getBallRadius() { return this.ballRadius; }
    public double getPaddleWidth() { return this.paddleWidth; }
    public BreakoutBrickSettings[] getBrickData() { return this.bricks; }
}
