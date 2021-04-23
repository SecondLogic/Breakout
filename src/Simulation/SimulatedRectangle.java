/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedRectangle.java
 */


package Simulation;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SimulatedRectangle extends SimulatedShape {
    private final Rectangle uiNode;

    public SimulatedRectangle(Vector2 size, Vector2 position) {
        Rectangle shape = new Rectangle();
        shape.setWidth(size.x);
        shape.setHeight(size.y);
        shape.setLayoutX((position.x - shape.getWidth()) / 2);
        shape.setLayoutY(position.y - shape.getHeight() / 2);
        shape.setFill(Color.WHITE);

        this.uiNode = shape;
        this.size = size;
        this.position = position;
    }

    @Override
    public Node getNode() {
        return this.uiNode;
    }
}