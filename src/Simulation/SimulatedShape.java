/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedShape.java
 */

package Simulation;

import Structures.BoundingBox;
import Structures.BoundedObject;
import Structures.Vector2;
import javafx.scene.shape.Shape;

public class SimulatedShape implements BoundedObject {
    protected Vector2 size, position, velocity;
    private BoundingBox bounds;
    private boolean changed;
    private boolean anchored;
    private double mass;
    private SimulationEventHandler<ShapeCollision> onCollide;

    public SimulatedShape() {
        this.size = Vector2.ZERO;
        this.position = Vector2.ZERO;
        this.velocity = Vector2.ZERO;
        this.bounds = new BoundingBox();
        this.changed = false;
        this.anchored = true;
        this.mass = 1;
        this.onCollide = null;
    }

    public SimulatedShape(Vector2 size, Vector2 position) {
        this.size = size;
        this.position = position;
        this.velocity = Vector2.ZERO;
        this.bounds = new BoundingBox(position.sum(size.product(-.5)), position.sum(size.product(.5)));
        this.anchored = true;
        this.mass = 1;
        this.onCollide = null;
    }

    public Shape getNode() {
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

    private void updateBounds() {
        this.bounds = new BoundingBox(position.sum(size.product(-.5)), position.sum(size.product(.5)));
    }

    public void moveTo(Vector2 position) {
        if (!position.equals(this.position)) {
            this.position = position;
            this.changed = true;
            updateBounds();
        }
    }

    public void resize(Vector2 size) {
        if (!size.equals(this.size)) {
            this.size = size;
            this.changed = true;
            updateBounds();
        }
    }

    public Vector2 getPosition() {
        return this.position;
    }

    public Vector2 getSize() {
        return this.size;
    }

    public BoundingBox getBounds() { return this.bounds; }

    public boolean consumeChangedFlag() {
        boolean flag = this.changed;
        this.changed = false;
        return flag;
    }

    public void triggerCollisionEvent(ShapeCollision collision) {
        if (this.onCollide != null) {
            this.onCollide.run(collision);
        }
    }
}
