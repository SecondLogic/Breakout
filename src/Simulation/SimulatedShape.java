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
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import javafx.scene.paint.Color;

public abstract class SimulatedShape implements BoundedObject {
    public enum CollisionType {CIRCLE, POLYGON};

    protected Vector2 size, position, velocity;
    protected Shape uiNode;
    public final CollisionType collisionType;
    private ObservableList<Node> nodeParent;
    private BoundingBox bounds;
    private boolean changed;
    private boolean anchored;
    private double mass, rotation;
    private SimulationEventHandler<ShapeCollision> onCollide;

    public SimulatedShape(Shape uiNode, Vector2 size, Vector2 position, CollisionType collisionType) {
        this.collisionType = collisionType;
        this.uiNode = uiNode;
        this.size = size;
        this.position = position;
        this.velocity = Vector2.ZERO;
        this.bounds = new BoundingBox(position.sum(size.product(-.5)), position.sum(size.product(.5)));
        this.anchored = true;
        this.mass = 1;
        this.rotation = 0;
        this.onCollide = null;
    }

    public void setAnchored(boolean anchored) {
        this.anchored = anchored;
    }

    public boolean isAnchored() { return this.anchored; }

    public void setNodeParent(ObservableList<Node> nodeParent) {
        if (this.nodeParent != null) {
            this.nodeParent.remove(this.uiNode);
        }

        this.nodeParent = nodeParent;

        if (this.nodeParent != null) {
            this.nodeParent.add(this.uiNode);
        }
    }

    private BoundingBox getShapeBounds() {
        return new BoundingBox(position.sum(size.product(-.5)), position.sum(size.product(.5)));
    }

    public void updateBoundsWithVelocity(double deltaTime) {
        if (this.velocity == Vector2.ZERO) {
            this.bounds = getShapeBounds();
        }
        else {
            BoundingBox defaultBounds = getShapeBounds();
            Vector2 delta = this.velocity.product(deltaTime);
            this.bounds = defaultBounds.expand(delta.sum(defaultBounds.min)).expand(delta.sum(defaultBounds.max));
        }
    }

    public BoundingBox getBounds() { return this.bounds; }

    public Vector2 getPosition() {
        return this.position;
    }

    public void moveTo(Vector2 position) {
        if (!position.equals(this.position)) {
            this.position = position;
            this.changed = true;
            this.bounds = getShapeBounds();
        }
    }

    public Vector2 getSize() {
        return this.size;
    }

    public void resize(Vector2 size) {
        if (!size.equals(this.size)) {
            this.size = size;
            this.changed = true;
            this.bounds = getShapeBounds();
        }
    }

    public void setColor(Color color) {
        Platform.runLater(() -> {
            uiNode.setFill(color);
        });
    }

    public Vector2 getVelocity() {
        return this.velocity;
    }

    public void setVelocity(Vector2 velocity) {
        this.velocity = velocity;
    }

    public double getMass() { return this.mass; }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getRotation() { return this.rotation; }

    public void setRotation(double rotation) { this.rotation = rotation % 360; }

    public void setOnCollide(SimulationEventHandler<ShapeCollision> collisionEvent) {
        this.onCollide = collisionEvent;
    }

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
