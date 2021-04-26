/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulationSpace.java
 */

package Simulation;

import Structures.RTree;
import Structures.Vector2;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class SimulationSpace {
    private ArrayList<SimulatedShape> shapes;
    private RTree<SimulatedShape> shapeRegions;
    private ObservableList<Node> uiChildren;
    private long lastSimulationTick;
    private boolean simulating;

    public SimulationSpace(Scene scene) {
        this.shapes = new ArrayList<>();
        this.shapeRegions = new RTree<>();
        this.uiChildren = ((Pane) scene.getRoot()).getChildren();
        this.resetTick();
    }

    public void add(SimulatedShape shape) {
        this.shapes.add(shape);
        this.shapeRegions.insert(shape);
        this.uiChildren.add(shape.getNode());
    }

    public void remove(SimulatedShape shape) {
        this.shapes.remove(shape);
        this.shapeRegions.remove(shape);
        this.uiChildren.remove(shape.getNode());
    }

    public void setScene(Scene scene) {
        ObservableList<Node> sceneChildren = ((Pane) scene.getRoot()).getChildren();
        for (SimulatedShape shape : this.shapes) {
            this.uiChildren.remove(shape.getNode());
            sceneChildren.add(shape.getNode());
        }
        this.uiChildren = sceneChildren;
    }

    public boolean isSimulating() {
        return this.simulating;
    }

    public void resetTick() {
        this.lastSimulationTick = System.currentTimeMillis();
    }

    public void simulate() {
        if (this.simulating) {
            return;
        }

        this.simulating = true;
        long currentTick = System.currentTimeMillis();

        // Check unanchored shapes for collision
        ArrayList<SimulatedShape> checkedShapes = new ArrayList<>();
        for (SimulatedShape shape : this.shapes) {
            shape.getNode().setFill(Color.WHITE);
        }

        for (SimulatedShape shape : this.shapes) {
            //shape.moveTo(shape.getPosition().sum(Math.sin(currentTick / 100.0),0));

            /*
            if (shape.consumeChangedFlag()) {

                this.shapes.remove(shape);
                this.shapes.insert(shape);
            }
             */

            if (!shape.isAnchored()) {
                // Get rough collision
                ArrayList<SimulatedShape> overlaps = this.shapeRegions.getObjectsInRegion(shape.getBounds());

                // Remove redundant checks
                checkedShapes.add(shape);
                overlaps.removeAll(checkedShapes);

                // Handle fine collision
                for (SimulatedShape overlappedShape : overlaps) {
                    ShapeCollision collision = ShapeCollision.collide(shape, overlappedShape);

                    if (collision.collided) {
                        shape.triggerCollisionEvent(collision);
                        overlappedShape.triggerCollisionEvent(collision);

                        shape.getNode().setFill(Color.RED);
                        overlappedShape.getNode().setFill(Color.RED);
                    }
                }
            }
        }

        this.lastSimulationTick = currentTick;
        this.simulating = false;
    }
}
