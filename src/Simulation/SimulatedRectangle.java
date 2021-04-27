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
import javafx.scene.shape.Shape;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SimulatedRectangle extends SimulatedShape {
    public SimulatedRectangle(Vector2 size, Vector2 position) {
        super(new Rectangle(), size, position);

        Rectangle shape = (Rectangle) this.uiNode;

        Platform.runLater(() -> {
            shape.setWidth(size.x);
            shape.setHeight(size.y);
            shape.setLayoutX(position.x - size.x / 2);
            shape.setLayoutY(position.y - size.y / 2);
            shape.setFill(Color.WHITE);
        });
    }

    public SimulatedRectangle(Vector2 size, Vector2 position, SimulationSpace space) {
        this(size, position);
        space.add(this);
    }

    @Override
    public void moveTo(Vector2 position) {
        super.moveTo(position);

        Platform.runLater(() -> {
            this.uiNode.setLayoutX(this.position.x - size.x / 2);
            this.uiNode.setLayoutY(this.position.y - size.y / 2);
        });
    }

    @Override
    public void resize(Vector2 size) {
        super.resize(size);

        Rectangle shape = (Rectangle) this.uiNode;
        Platform.runLater(() -> {
            shape.setWidth(size.x);
            shape.setHeight(size.y);
            shape.setLayoutX(this.position.x - size.x / 2);
            shape.setLayoutY(this.position.y - size.y / 2);
        });
    }
}