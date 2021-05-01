/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/ShapeCollision.java
 */


package Simulation;

import Structures.Vector2;

import java.util.ArrayList;

public class ShapeCollision extends SimulationEvent {
    public static boolean collide(SimulatedShape shape0, SimulatedShape shape1, double deltaTime) {
        Vector2 collisionAxis = Vector2.ZERO;

        // Get relative velocity of shape0 towards shape1
        Vector2 relVelocity = shape0.velocity.sum(shape1.velocity.product(-1));

        // Objects wont collide if they are stationary relative to each other
        if (relVelocity == Vector2.ZERO) {
            return false;
        }

        // Get collision axis and speed
        Vector2 normalAxis = relVelocity.left();
        double speed = relVelocity.magnitude();

        ArrayList<Vector2> shape0Sweep = new ArrayList<>();


        shape0.triggerCollisionEvent(new ShapeCollision(collisionAxis, shape1));
        shape1.triggerCollisionEvent(new ShapeCollision(collisionAxis, shape0));

        return false;
    }

    public final Vector2 collisionAxis;
    public final SimulatedShape collidedShape;

    private ShapeCollision(Vector2 collisionAxis, SimulatedShape collidedShape) {
        this.collisionAxis = collisionAxis;
        this.collidedShape = collidedShape;
    }
}
