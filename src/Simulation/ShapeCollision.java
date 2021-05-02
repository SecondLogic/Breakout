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
        this.local = this.world.sum(origin.product(-1)).toLocalSpace(localSpace);
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

        SweepPoint point = maxX;
        int sweepDir = 1;

        // Sweep to find max X and possible Y extrema
        while (true) {
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
            if ((!invertX && point.local.x >= maxX.local.x) || (invertX && point.local.x <= maxX.local.x)) {
                maxX = point;
                maxXChanged = true;
            }

            // Set max Y
            if (point.local.y > maxY.local.y || (point.local.y == maxY.local.y && maxXChanged)) {
                maxY = point;
                if (!maxXChanged) {
                    sweepDir = -1;
                    point = maxX;
                }
            }

            // Set min Y
            else if (point.local.y < minY.local.y || (point.local.y == minY.local.y && maxXChanged)) {
                minY = point;
                if (!maxXChanged) {
                    sweepDir = -1;
                    point = maxX;
                }
            }

            // Stop sweep if nothing changed
            else if (!maxXChanged) {
                if (sweepDir == -1) {
                    break;
                }
                else {
                    sweepDir = -1;
                    point = maxX;
                }
            }
        }

        // Sweep outwards to find actual Y extrema
        while (true) {
            SweepPoint oldPoint = maxY;
            int sweepIndex;
            if (invertX) {
                point = maxY.left();
                sweepIndex = (oldPoint.sweepIndex - 1) % points.length;
            }
            else {
                point = maxY.right();
                sweepIndex = (oldPoint.sweepIndex + 1) % points.length;
            }
            if (point == null) {
                if (sweepIndex < 0) {
                    sweepIndex += points.length;
                }
                point = new SweepPoint(points[sweepIndex], shape.position, origin, shape.getRotation(), relVelocity, sweepIndex);
                if (point.local.y > maxY.local.y) {
                    maxY = point;
                    if (invertX) {
                        oldPoint.setLeft(point);
                        point.setRight(oldPoint);
                    }
                    else {
                        oldPoint.setRight(point);
                        point.setLeft(oldPoint);
                    }
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
            SweepPoint oldPoint = minY;
            int sweepIndex;
            if (invertX) {
                point = minY.right();
                sweepIndex = (oldPoint.sweepIndex + 1) % points.length;
            }
            else {
                point = minY.left();
                sweepIndex = (oldPoint.sweepIndex - 1) % points.length;
            }
            if (point == null) {
                if (sweepIndex < 0) {
                    sweepIndex += points.length;
                }
                point = new SweepPoint(points[sweepIndex], shape.position, origin, shape.getRotation(), relVelocity, sweepIndex);
                if (point.local.y < minY.local.y) {
                    minY = point;
                    if (invertX) {
                        oldPoint.setRight(point);
                        point.setLeft(oldPoint);
                    }
                    else {
                        oldPoint.setLeft(point);
                        point.setRight(oldPoint);
                    }
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
        if (invertX) {
            maxY.setLeft(null);
            minY.setRight(null);
        }
        else {
            maxY.setRight(null);
            minY.setLeft(null);
        }

        return new SweepPoint[] {minY, maxY, maxX};
    }

    public static boolean collide(SimulatedShape shape0, SimulatedShape shape1, double deltaTime) {
        // Get relative velocity of shape0 towards shape1
        Vector2 relVelocity = shape0.velocity.sum(shape1.velocity.product(-1)).product(deltaTime);

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

            // No collision if extrema along the line of displacement are farther apart than maxDisplacement
            if (shape1Ex[2].local.x - shape0Ex[2].local.x > maxDisplacement) {
                return false;
            }

            // No collision if shapes don't overlap along the local Y axis
            if (shape0Ex[0].local.y >= shape1Ex[1].local.y || shape1Ex[0].local.y >= shape0Ex[1].local.y) {
                return false;
            }

            // Get points of contact
            ArrayList<Vector2> contactPoints = new ArrayList<>();
            double minDistance = maxDisplacement;
            Vector2 collisionAxis = relVelocity.left().unit();

            SweepPoint point = shape0Ex[0];
            SweepPoint edge0 = shape1Ex[0];
            SweepPoint edge1 = edge0.left();
            while (point != shape0Ex[1].right()) {
                double distance = minDistance + 1;
                if (point.local.y > edge0.local.y) {
                    while (edge1 != null && point.local.y > edge1.local.y) {
                        edge0 = edge1;
                        edge1 = edge0.left();
                    }
                    if (edge1 != null) {
                        double edgeAlpha = (point.local.y - edge0.local.y) / (edge1.local.y - edge0.local.y);
                        distance = (edge0.local.x * (1 - edgeAlpha) + edge1.local.x * edgeAlpha) - point.local.x;
                    }
                }
                else if (point.local.y == edge0.local.y) {
                    distance = edge0.local.x - point.local.x;
                }
                if (distance <= minDistance) {
                    if (distance < minDistance) {
                        contactPoints.clear();
                        minDistance = distance;
                    }
                    double velocityAlpha = minDistance / maxDisplacement;
                    contactPoints.add(point.world.sum(shape0.velocity.product(velocityAlpha * deltaTime)));
                }
                point = point.right();
            }

            point = shape1Ex[0];
            edge0 = shape0Ex[0];
            edge1 = edge0.right();
            while (point != shape1Ex[1].left()) {
                double distance = minDistance + 1;
                if (point.local.y > edge0.local.y) {
                    while (edge1 != null && point.local.y > edge1.local.y) {
                        edge0 = edge1;
                        edge1 = edge0.right();
                    }
                    if (edge1 != null) {
                        double edgeAlpha = (point.local.y - edge0.local.y) / (edge1.local.y - edge0.local.y);
                        distance = point.local.x - (edge0.local.x * (1 - edgeAlpha) + edge1.local.x * edgeAlpha);
                        collisionAxis = edge1.local.sum(edge0.local.product(-1)).unit();
                    }
                }
                else if (point.local.y == edge0.local.y) {
                    distance = point.local.x - edge0.local.x;
                }
                boolean collideThreshold = Math.abs(distance - minDistance) < 1;
                if (distance <= minDistance || collideThreshold) {
                    if (distance < minDistance && !collideThreshold) {
                        contactPoints.clear();
                    }
                    minDistance = Math.min(distance, minDistance);
                    double velocityAlpha = minDistance / maxDisplacement;
                    contactPoints.add(point.world.sum(shape0.velocity.product(velocityAlpha * deltaTime)));
                }
                point = point.left();
            }

            // Trigger collision if contact points exist
            if (contactPoints.size() > 0) {
                Vector2 averagePoint = Vector2.ZERO;
                for (Vector2 contactPoint : contactPoints) {
                    averagePoint = averagePoint.sum(contactPoint);
                }
                averagePoint.product(1 / contactPoints.size());

                if (contactPoints.size() > 1) {
                    collisionAxis = contactPoints.get(0).sum(contactPoints.get(1).product(-1)).unit();
                }

                double velocityAlpha = minDistance / maxDisplacement;
                double leftOverTime = (1 - velocityAlpha) * deltaTime;

                shape0.moveTo(shape0.position.sum(shape0.velocity.product(deltaTime * velocityAlpha)));
                shape1.moveTo(shape1.position.sum(shape1.velocity.product(deltaTime * velocityAlpha)));

                shape0.triggerCollisionEvent(new ShapeCollision(averagePoint, collisionAxis, shape1, leftOverTime));
                shape1.triggerCollisionEvent(new ShapeCollision(averagePoint, collisionAxis, shape0, leftOverTime));

                System.out.println(contactPoints.size() + ", " + minDistance + ", " + maxDisplacement + ", " + velocityAlpha);
                System.out.println(averagePoint);
                System.out.println(collisionAxis);
                return true;
            }
            return false;
        }
        else {
            shape0.triggerCollisionEvent(new ShapeCollision(shape0.position, shape0.velocity.left().unit(), shape1, deltaTime));
            shape1.triggerCollisionEvent(new ShapeCollision(shape1.position, shape1.velocity.left().unit(), shape0, deltaTime));
        }

        return false;
    }

    public final Vector2 collisionPoint, collisionAxis;
    public final SimulatedShape collidedShape;
    public final double leftOverTime;

    private ShapeCollision(Vector2 collisionPoint, Vector2 collisionAxis, SimulatedShape collidedShape, double leftOverTime) {
        this.collisionPoint = collisionPoint;
        this.collisionAxis = collisionAxis;
        this.collidedShape = collidedShape;
        this.leftOverTime = leftOverTime;
    }
}
