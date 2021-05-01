/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedRectangle.java
 */


package Simulation;

import Structures.Vector2;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class SimulatedCircle extends SimulatedShape {
    public SimulatedCircle(double radius, Vector2 position) {
        super(new Circle(), new Vector2(radius * 2, radius * 2), position);

        Circle shape = (Circle) this.uiNode;

        Platform.runLater(() -> {
            shape.setRadius(radius);
            shape.setCenterX(position.x);
            shape.setCenterY(position.y);
            shape.setFill(Color.WHITE);
        });
    }

    public SimulatedCircle(double radius, Vector2 position, SimulationSpace space) {
        this(radius, position);
        space.add(this);
    }

    @Override
    public void moveTo(Vector2 position) {
        super.moveTo(position);

        Circle shape = (Circle) this.uiNode;
        Platform.runLater(() -> {
            shape.setCenterX(position.x);
            shape.setCenterY(position.y);
        });
    }

    public void resize(double radius) {
        super.resize(new Vector2(radius * 2, radius * 2));

        Circle shape = (Circle) this.uiNode;
        Platform.runLater(() -> {
            shape.setRadius(radius);
            shape.setCenterX(position.x);
            shape.setCenterY(position.y);
        });
    }

    @Override
    public void resize(Vector2 size) {
        this.resize(size.x / 2);
    }
}