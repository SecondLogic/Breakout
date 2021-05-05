/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulationSpace.java
 */

package Simulation;

import Structures.RTree;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class SimulationSpace {
    private ArrayList<SimulatedShape> shapes, addQueue, removeQueue;
    private RTree<SimulatedShape> shapeRegions;
    private ObservableList<Node> uiChildren;
    private long lastSimulationTick;
    private boolean simulating;

    public SimulationSpace(Scene scene) {
        this.shapes = new ArrayList<>();
        this.addQueue = new ArrayList<>();
        this.removeQueue = new ArrayList<>();
        this.shapeRegions = new RTree<>(4,4);
        this.uiChildren = ((Pane) scene.getRoot()).getChildren();
        this.resetTick();
    }

    public void add(SimulatedShape shape) {
        if (!this.simulating) {
            this.shapes.add(shape);
            this.shapeRegions.insert(shape);
            shape.setNodeParent(this.uiChildren);
        }
        else {
            addQueue.add(shape);
        }
    }

    public void remove(SimulatedShape shape) {
        if (!this.simulating) {
            this.shapes.remove(shape);
            this.shapeRegions.remove(shape);
            shape.setNodeParent(null);
        }
        else {
            removeQueue.add(shape);
        }
    }

    public void setScene(Scene scene) {
        ObservableList<Node> sceneChildren = ((Pane) scene.getRoot()).getChildren();
        this.uiChildren = sceneChildren;
        for (SimulatedShape shape : this.shapes) {
            shape.setNodeParent(this.uiChildren);
        }
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
        double deltaTime = (currentTick - lastSimulationTick) / 1000.0;

        // Update RTree to reflect changes
        for (SimulatedShape shape : this.shapes) {
            if (!shape.isAnchored()) {
                shape.updateBoundsWithVelocity(deltaTime);
            }

            if (shape.consumeChangedFlag()) {
                this.shapeRegions.remove(shape);
                this.shapeRegions.insert(shape);
            }
        }

        // Check unanchored shapes for collision
        ArrayList<SimulatedShape> checkedShapes = new ArrayList<>();

        for (SimulatedShape shape : this.shapes) {
            if (!shape.isAnchored()) {
                // Get rough collision
                ArrayList<SimulatedShape> overlaps = this.shapeRegions.getObjectsInRegion(shape.getBounds());

                // Remove redundant checks
                checkedShapes.add(shape);
                overlaps.removeAll(checkedShapes);

                // Handle fine collision
                boolean collided = false;
                for (SimulatedShape overlappedShape : overlaps) {
                    collided = collided || ShapeCollision.collide(shape, overlappedShape, deltaTime);
                }

                if (!collided) {
                    shape.moveTo(shape.getPosition().sum(shape.getVelocity().product(deltaTime)));
                }
            }
        }

        this.lastSimulationTick = currentTick;
        this.simulating = false;

        // Perform waiting tasks
        for (SimulatedShape shape : addQueue) {
            this.add(shape);
        }
        addQueue.clear();

        for (SimulatedShape shape : removeQueue) {
            this.remove(shape);
        }
        removeQueue.clear();
    }
}
