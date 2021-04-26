/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedRectangle.java
 */


package Simulation;

import Structures.Vector2;
import javafx.scene.shape.Shape;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SimulatedRectangle extends SimulatedShape {
    private final Rectangle uiNode;

    public SimulatedRectangle(Vector2 size, Vector2 position) {
        super(size, position);

        Rectangle shape = new Rectangle();
        shape.setWidth(size.x);
        shape.setHeight(size.y);
        shape.setLayoutX(position.x - size.x / 2);
        shape.setLayoutY(position.y - size.y / 2);
        shape.setFill(Color.WHITE);

        this.uiNode = shape;
    }

    public SimulatedRectangle(Vector2 size, Vector2 position, SimulationSpace space) {
        this(size, position);
        space.add(this);
    }

    @Override
    public void moveTo(Vector2 position) {
        super.moveTo(position);

        this.uiNode.setLayoutX(this.position.x - size.x / 2);
        this.uiNode.setLayoutY(this.position.y - size.y / 2);
    }

    @Override
    public void resize(Vector2 size) {
        super.resize(size);
        this.uiNode.setWidth(size.x);
        this.uiNode.setHeight(size.y);
        this.uiNode.setLayoutX(this.position.x - size.x / 2);
        this.uiNode.setLayoutY(this.position.y - size.y / 2);
    }

    @Override
    public Shape getNode() {
        return this.uiNode;
    }
}