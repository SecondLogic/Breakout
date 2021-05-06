/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/05/2021

    Breakout
    Game/BreakoutBrick.java
 */

package Game;

import Simulation.SimulatedRectangle;
import Structures.Vector2;
import javafx.application.Platform;
import javafx.scene.paint.Color;

class BreakoutBrickSettings {
    public final Vector2 size;
    public final Vector2 position;
    public final double rotation;
    public final int health;

    public BreakoutBrickSettings(Vector2 size, Vector2 position, double rotation, int health) {
        this.size = size;
        this.position = position;
        this.rotation = rotation;
        this.health = health;
    }
}
public class BreakoutBrick extends SimulatedRectangle {
    private BreakoutRoom room;
    private int health;

    public BreakoutBrick(BreakoutBrickSettings brickSettings, BreakoutRoom room) {
        super(brickSettings.size, brickSettings.position);
        this.room = room;
        this.health = brickSettings.health;
        this.setRotation(brickSettings.rotation);

        this.setOnCollide(collision -> {
            if (collision.collidedShape instanceof BreakoutBall) {
                BreakoutBall ball = (BreakoutBall) collision.collidedShape;

                if (ball.getLastCollided() != this) {
                    this.health -= 1;
                    if (this.health <= 0) {
                        this.room.remove(this);
                    } else {
                        this.updateColor();
                    }
                }
            }
        });

        this.room.add(this);
    }

    public void updateColor() {
        String color;
        switch (this.health) {
            case 1:
                color = "#0077FF";
                break;
            case 2:
                color = "#00FF77";
                break;
            case 3:
                color = "#FFF000";
                break;
            case 4:
                color = "#FF7700";
                break;
            case 5:
                color = "#FF0000";
                break;
            case 6:
                color = "#AA00FF";
                break;
            case 7:
                color = "#FF00FF";
                break;
            default:
                color = "#FFFFFF";
                break;
        }

        Platform.runLater(() -> {
            this.uiNode.setFill(Color.valueOf(color));
        });
    }
}
