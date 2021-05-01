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

class SweepPoint {
    private SweepPoint left, right;
    public final Vector2 world, local;
    public int sweepIndex;

    public SweepPoint(Vector2 meshPoint, Vector2 position, Vector2 origin, double rotation, Vector2 localSpace, int sweepIndex) {
        meshPoint = meshPoint.rotate(rotation);
        this.world = meshPoint.sum(position);
        this.local = meshPoint.sum(position.sum(origin.product(-1))).toLocalSpace(localSpace);
        this.sweepIndex = sweepIndex;
        this.left = null;
        this.right = null;
    }

    public SweepPoint left() {
        return this.left;
    }

    public SweepPoint right() {
        return this.right;
    }

    public void setLeft(SweepPoint left) {
        this.left = left;
    }

    public void setRight(SweepPoint right) {
        this.right = right;
    }
}

public class ShapeCollision extends SimulationEvent {
    private static SweepPoint[] sweepShape(SimulatedPolygon shape, Vector2 origin, Vector2 relVelocity, boolean invertX) {
        Vector2[] points = shape.getPoints();

        SweepPoint maxX = new SweepPoint(points[0], shape.position, origin, shape.getRotation(), relVelocity, 0);
        SweepPoint minY = maxX;
        SweepPoint maxY = maxX;

        boolean stopMaxSweep = false;
        boolean stopMinSweep = false;
        SweepPoint point = maxX;
        int sweepDir = 1;

        // Sweep to find max X and possible Y extrema
        while (!(stopMaxSweep && stopMinSweep)) {
            // Get next point
            int sweepIndex = (point.sweepIndex + sweepDir) % points.length;
            if (sweepIndex < 0) {
                sweepIndex += points.length;
            }
            if (sweepDir == 1) {
                if (point.right() != null) {
                    point = point.right();
                } else {
                    SweepPoint oldPoint = point;
                    point = new SweepPoint(points[sweepIndex], shape.position, origin, shape.getRotation(), relVelocity, sweepIndex);
                    oldPoint.setRight(point);
                    point.setLeft(oldPoint);
                }
            }
            else {
                if (point.left() != null) {
                    point = point.left();
                } else {
                    SweepPoint oldPoint = point;
                    point = new SweepPoint(points[sweepIndex], shape.position, origin, shape.getRotation(), relVelocity, sweepIndex);
                    oldPoint.setLeft(point);
                    point.setRight(oldPoint);
                }
            }

            // Set max X
            boolean maxXChanged = false;
            if ((!invertX && point.local.x > maxX.local.x) || (invertX && point.local.x < maxX.local.x)) {
                maxX = point;
                maxXChanged = true;
            }

            // Set max Y
            if (!stopMaxSweep && (point.local.y > maxY.local.y || (point.local.y == maxY.local.y && maxXChanged))) {
                maxY = point;
                if (!maxXChanged) {
                    stopMaxSweep = true;
                    sweepDir = -1;
                    point = maxX;
                }
            }

            // Set min Y
            else if (!stopMinSweep && (point.local.y < minY.local.y || (point.local.y == minY.local.y && maxXChanged))) {
                minY = point;
                if (!maxXChanged) {
                    stopMinSweep = true;
                    sweepDir = -1;
                    point = maxX;
                }
            }

            // Stop sweep if nothing changed
            else if (!maxXChanged) {
                stopMaxSweep = true;
                stopMinSweep = true;
            }
        }

        // Sweep outwards to find actual Y extrema
        while (true) {
            SweepPoint oldPoint = point;
            point = maxY.left();
            if (point == null) {
                int sweepIndex = (oldPoint.sweepIndex - 1) % points.length;
                if (sweepIndex < 0) {
                    sweepIndex += points.length;
                }
                point = new SweepPoint(points[sweepIndex], shape.position, origin, shape.getRotation(), relVelocity, sweepIndex);
                if (point.local.y > maxY.local.y) {
                    maxY = point;
                    oldPoint.setLeft(point);
                    point.setRight(oldPoint);
                }
                else {
                    break;
                }
            }
            else {
                break;
            }
        }

        while (true) {
            SweepPoint oldPoint = point;
            point = minY.right();
            if (point == null) {
                int sweepIndex = (oldPoint.sweepIndex + 1) % points.length;
                point = new SweepPoint(points[sweepIndex], shape.position, origin, shape.getRotation(), relVelocity, sweepIndex);
                if (point.local.y < minY.local.y) {
                    minY = point;
                    oldPoint.setRight(point);
                    point.setLeft(oldPoint);
                }
                else {
                    break;
                }
            }
            else {
                break;
            }
        }

        // Trim sweep result to extrema
        maxY.setLeft(null);
        minY.setRight(null);

        return new SweepPoint[] {minY, maxY, maxX};
    }

    public static boolean collide(SimulatedShape shape0, SimulatedShape shape1, double deltaTime) {
        Vector2 collisionPoint = Vector2.ZERO;

        // Get relative velocity of shape0 towards shape1
        Vector2 relVelocity = shape0.velocity.sum(shape1.velocity.product(-1));

        // Objects wont collide if they are stationary relative to each other
        if (relVelocity == Vector2.ZERO) {
            return false;
        }

        double maxDisplacement = relVelocity.magnitude();

        // POLYGON-POLYGON collision
        if (shape0 instanceof SimulatedPolygon && shape1 instanceof SimulatedPolygon) {
            // Sweep both objects in the frame of relVelocity
            SweepPoint[] shape0Ex = sweepShape((SimulatedPolygon) shape0, shape0.position, relVelocity, false);
            SweepPoint[] shape1Ex = sweepShape((SimulatedPolygon) shape1, shape0.position, relVelocity, true);

            // No collision if extrema along the line of displacement are farther than maxDisplacement
            if (shape1Ex[2].local.x - shape0Ex[2].local.x > maxDisplacement) {
                return false;
            }

            // Trim sweep to overlapping bounds
            if (shape0Ex[0].local.y > shape1Ex[0].local.y) {
                do {
                    shape1Ex[0] = shape1Ex[0].left();
                    // No overlap: no collision
                    if (shape1Ex[0] == null) {
                        return false;
                    }
                } while(shape0Ex[0].local.y > shape1Ex[0].local.y);
            }
            else {
                while(shape1Ex[0].local.y > shape0Ex[0].local.y) {
                    shape0Ex[0] = shape0Ex[0].left();
                    // No overlap: no collision
                    if (shape0Ex[0] == null) {
                        return false;
                    }
                }
            }

            if (shape0Ex[1].local.y < shape1Ex[1].local.x) {
                do {
                    shape1Ex[1] = shape1Ex[1].right();
                    // No overlap: no collision
                    if (shape1Ex[1] == null) {
                        return false;
                    }
                } while(shape0Ex[1].local.y > shape1Ex[1].local.x);

            }
            else {
                while(shape1Ex[1].local.y < shape0Ex[1].local.y) {
                    shape0Ex[1] = shape0Ex[1].right();
                    // No overlap: no collision
                    if (shape0Ex[1] == null) {
                        return false;
                    }
                }
            }

            // Get points of contact
            ArrayList<Vector2> contactPoints = new ArrayList<>();
            double minDistance = maxDisplacement;

            SweepPoint point = shape0Ex[0];
            SweepPoint edge0 = shape1Ex[0];
            SweepPoint edge1 = edge0.left();
            while (point != shape0Ex[1].left()) {
                while (edge1 != null && edge1.local.y < point.local.y) {
                    edge0 = edge1;
                    edge1 = edge0.left();
                }
                if (edge1 != null) {
                    double edgeAlpha = (point.local.y - edge0.local.y) / (edge1.local.y - edge0.local.y);
                    double edgeX = edge0.local.x * (1 - edgeAlpha) + edge1.local.x * edgeAlpha;
                    double distance = edgeX - point.local.x;

                    if (distance <= minDistance) {
                        if (distance < minDistance) {
                            contactPoints.clear();
                            minDistance = distance;
                        }
                        double velocityAlpha = distance / maxDisplacement;
                        contactPoints.add(point.world.sum(shape0.velocity.product(velocityAlpha)));
                    }

                    point = point.left();
                }
                else {
                    break;
                }
            }

            point = shape1Ex[0];
            edge0 = shape0Ex[0];
            edge1 = edge0.left();
            while (point != shape1Ex[1].left()) {
                while (edge1 != null && edge1.local.y < point.local.y) {
                    edge0 = edge1;
                    edge1 = edge0.left();
                }
                if (edge1 != null) {
                    double edgeAlpha = (point.local.y - edge0.local.y) / (edge1.local.y - edge0.local.y);
                    double edgeX = edge0.local.x * (1 - edgeAlpha) + edge1.local.x * edgeAlpha;
                    double distance = point.local.x - edgeX;

                    if (distance <= minDistance) {
                        if (distance < minDistance) {
                            contactPoints.clear();
                            minDistance = distance;
                        }
                        double velocityAlpha = distance / maxDisplacement;
                        contactPoints.add(point.world.sum(shape1.velocity.product(velocityAlpha)));
                    }

                    point = point.left();
                } else {
                    break;
                }
            }

            // Trigger collision if contact points exist
            if (contactPoints.size() > 0) {
                Vector2 averagePoint = Vector2.ZERO;
                for (Vector2 contactPoint : contactPoints) {
                    averagePoint = averagePoint.sum(contactPoint);
                }
                averagePoint.product(1 / contactPoints.size());
                shape0.triggerCollisionEvent(new ShapeCollision(averagePoint, shape1));
                shape1.triggerCollisionEvent(new ShapeCollision(averagePoint, shape0));
                System.out.println(contactPoints.size());
                System.out.println(averagePoint);
                return true;
            }
            return false;
        }
        else {
            shape0.triggerCollisionEvent(new ShapeCollision(collisionPoint, shape1));
            shape1.triggerCollisionEvent(new ShapeCollision(collisionPoint, shape0));
        }

        return false;
    }

    public final Vector2 collisionPoint;
    public final SimulatedShape collidedShape;

    private ShapeCollision(Vector2 collisionPoint, SimulatedShape collidedShape) {
        this.collisionPoint = collisionPoint;
        this.collidedShape = collidedShape;
    }
}
