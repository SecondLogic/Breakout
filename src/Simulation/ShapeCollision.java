/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/ShapeCollision.java
 */


package Simulation;

public class ShapeCollision extends SimulationEvent {
    public static ShapeCollision collide(SimulatedShape shape0, SimulatedShape shape1) {
        if (shape0.isAnchored() && shape1.isAnchored()) {
            return new ShapeCollision(false, Vector2.ZERO, shape0, shape1);
        }

        // COLLIDE SHAPES

        return new ShapeCollision(true, Vector2.ZERO, shape0, shape1);
    }

    public final boolean collided;
    public final Vector2 collisionAxis;
    public final SimulatedShape shape0;
    public final SimulatedShape shape1;

    private ShapeCollision(boolean collided, Vector2 collisionAxis, SimulatedShape shape0, SimulatedShape shape1) {
        this.collided = collided;
        this.collisionAxis = collisionAxis;
        this.shape0 = shape0;
        this.shape1 = shape1;
    }
}
