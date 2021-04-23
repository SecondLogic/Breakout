/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulationSpace.java
 */

package Simulation;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class SimulationSpace extends RTree {
    private ObservableList<Node> uiChildren;
    private long lastSimulationTick;

    public SimulationSpace(Scene scene) {
        super(3, 4);
        this.uiChildren = ((Pane) scene.getRoot()).getChildren();
        this.resetTick();
    }

    public void add(SimulatedShape shape) {
        super.insert(shape);
        uiChildren.add(shape.getNode());
    }

    public void remove(SimulatedShape shape) {
        super.remove(shape);
        uiChildren.remove(shape.getNode());
    }

    public void setScene(Scene scene) {
        ObservableList<Node> sceneChildren = ((Pane) scene.getRoot()).getChildren();
        for (SimulatedShape shape : this.root.getShapes()) {
            this.uiChildren.remove(shape.getNode());
            sceneChildren.add(shape.getNode());
        }
        this.uiChildren = sceneChildren;
    }

    public void resetTick() {
        this.lastSimulationTick = System.currentTimeMillis();
    }

    public void simulate() {
        long currentTick = System.currentTimeMillis();

        ArrayList<SimulatedShape> checkedShapes = new ArrayList<>();
        for (SimulatedShape shape : this.root.getShapes()) {
            if (!shape.isAnchored()) {
                checkedShapes.add(shape);
                ArrayList<SimulatedShape> overlaps = this.getOverlapping(shape);
                overlaps.removeAll(checkedShapes);

                for (SimulatedShape overlappedShape : overlaps) {
                    ShapeCollision collision = ShapeCollision.collide(shape, overlappedShape);

                    if (collision.collided) {
                        shape.triggerCollisionEvent(collision);
                        overlappedShape.triggerCollisionEvent(collision);
                    }
                }
            }
        }

        this.lastSimulationTick = currentTick;
    }
}
