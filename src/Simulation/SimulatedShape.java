/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedShape.java
 */

package Simulation;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;

public class SimulatedShape extends BoundingBox {
    protected Vector2 size, position, velocity;
    private boolean anchored;
    private double mass;
    private SimulationEventHandler<ShapeCollision> onCollide;

    public SimulatedShape() {
        this.size = Vector2.ZERO;
        this.position = Vector2.ZERO;
        this.velocity = Vector2.ZERO;
        this.anchored = true;
        this.mass = 1;
        this.onCollide = null;
    }

    public Node getNode() {
        return null;
    }

    public void setAnchored(boolean anchored) {
        this.anchored = anchored;
    }

    public boolean isAnchored() { return this.anchored; }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public void setOnCollide(SimulationEventHandler<ShapeCollision> collisionEvent) {
        this.onCollide = collisionEvent;
    }

    public void triggerCollisionEvent(ShapeCollision collision) {
        if (this.onCollide != null) {
            this.onCollide.run(collision);
        }
    }
}
