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
        // Get points (clockwise order)
        Vector2[] points = shape.getPoints();

        // Create first sweep point
        SweepPoint maxX = new SweepPoint(points[0], shape.position, origin, shape.getRotation(), relVelocity, 0);
        SweepPoint minY = maxX;
        SweepPoint maxY = maxX;

        SweepPoint point = maxX;
        int sweepDir = 1;

        // Sweep to find max X and possible Y extrema
        while (true) {
            // Get or create next point
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

        // Sweep outwards to find actual Y Max
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

        // Sweep outwards to find actual Y Min
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
        // Set circle as second object (proper order when case is Polygon-Circle)
        if (shape0 instanceof SimulatedCircle) {
            SimulatedShape swapShape = shape0;
            shape0 = shape1;
            shape1 = swapShape;
        }

        // Get relative velocity of shape0 towards shape1
        Vector2 relVelocity = shape0.velocity.sum(shape1.velocity.product(-1)).product(deltaTime);

        // Objects wont collide if they are stationary relative to each other
        if (relVelocity == Vector2.ZERO) {
            return false;
        }

        double maxDisplacement = relVelocity.magnitude();

        // Choose proper collision case
        if (shape0 instanceof SimulatedPolygon) {
            // Sweep first shape in the frame of relVelocity
            SweepPoint[] shape0Ex = sweepShape((SimulatedPolygon) shape0, shape0.position, relVelocity, false);

            // POLYGON-POLYGON collision
            if (shape1 instanceof SimulatedPolygon) {
                // Sweep second shape
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
                Vector2 collisionAxis = relVelocity.normal().unit();

                // Test points on first shape
                SweepPoint point = shape0Ex[0];
                SweepPoint edge0 = shape1Ex[0];
                SweepPoint edge1 = edge0.left();
                while (point != shape0Ex[1].right()) {
                    double distance = minDistance + 1;
                    Vector2 axis = null;

                    // Test point-edge contact
                    if (point.local.y > edge0.local.y) {
                        while (edge1 != null && point.local.y > edge1.local.y) {
                            edge0 = edge1;
                            edge1 = edge0.left();
                        }
                        if (edge1 != null) {
                            double edgeAlpha = (point.local.y - edge0.local.y) / (edge1.local.y - edge0.local.y);
                            distance = (edge0.local.x * (1 - edgeAlpha) + edge1.local.x * edgeAlpha) - point.local.x;
                            axis = edge1.world.sum(edge0.world.product(-1)).unit();
                        }
                    }

                    // Test point-point contact
                    else if (point.local.y == edge0.local.y) {
                        distance = edge0.local.x - point.local.x;
                    }

                    // Set closest contact
                    if (distance <= minDistance) {
                        if (distance < minDistance) {
                            contactPoints.clear();
                            minDistance = distance;
                            collisionAxis = axis;
                        }
                        double velocityAlpha = minDistance / maxDisplacement;
                        contactPoints.add(point.world.sum(shape0.velocity.product(velocityAlpha * deltaTime)));
                    }
                    point = point.right();
                }

                // Test points on second shape
                point = shape1Ex[0];
                edge0 = shape0Ex[0];
                edge1 = edge0.right();
                while (point != shape1Ex[1].left()) {
                    double distance = minDistance + 1;
                    Vector2 axis = null;

                    // Test point-edge contact
                    if (point.local.y > edge0.local.y) {
                        while (edge1 != null && point.local.y > edge1.local.y) {
                            edge0 = edge1;
                            edge1 = edge0.right();
                        }
                        if (edge1 != null) {
                            double edgeAlpha = (point.local.y - edge0.local.y) / (edge1.local.y - edge0.local.y);
                            distance = point.local.x - (edge0.local.x * (1 - edgeAlpha) + edge1.local.x * edgeAlpha);
                            axis = edge1.world.sum(edge0.world.product(-1)).unit();
                        }
                    }

                    // Test point-point contact
                    else if (point.local.y == edge0.local.y) {
                        distance = point.local.x - edge0.local.x;
                    }

                    // Set closest contact
                    if (distance <= minDistance) {
                        if (distance < minDistance) {
                            contactPoints.clear();
                            minDistance = distance;
                            collisionAxis = axis;
                        }
                        double velocityAlpha = minDistance / maxDisplacement;
                        contactPoints.add(point.world.sum(shape0.velocity.product(velocityAlpha * deltaTime)));
                    }
                    point = point.left();
                }

                // Trigger collision if contact points exist
                if (contactPoints.size() > 0) {
                    // Determine average contact point
                    Vector2 averagePoint = Vector2.ZERO;
                    double minX = contactPoints.get(0).x;
                    double minY = contactPoints.get(0).y;
                    double maxX = minX;
                    double maxY = minY;
                    for (Vector2 contactPoint : contactPoints) {
                        minX = Math.min(minX, contactPoint.x);
                        minY = Math.min(minY, contactPoint.y);
                        maxX = Math.max(maxX, contactPoint.x);
                        maxY = Math.max(maxY, contactPoint.y);
                        averagePoint = averagePoint.sum(contactPoint);
                    }
                    averagePoint = averagePoint.product(1 / contactPoints.size());

                    if (contactPoints.size() > 2) {
                        collisionAxis = contactPoints.get(1).sum(contactPoints.get(0).product(-1)).unit();
                    }

                    // Update shapes
                    double velocityAlpha = minDistance / maxDisplacement;
                    double leftOverTime = (1 - velocityAlpha) * deltaTime;

                    shape0.moveTo(shape0.position.sum(shape0.velocity.product(deltaTime * velocityAlpha)).sum(shape0.velocity.unit().product(-.1)));
                    shape1.moveTo(shape1.position.sum(shape1.velocity.product(deltaTime * velocityAlpha)).sum(shape1.velocity.unit().product(-.1)));

                    // Trigger collision events
                    if (collisionAxis == null) {
                        shape0.triggerCollisionEvent(new ShapeCollision(averagePoint, averagePoint.sum(shape0.position.product(-1)).normal().unit(), shape1, leftOverTime));
                        shape1.triggerCollisionEvent(new ShapeCollision(averagePoint, averagePoint.sum(shape1.position.product(-1)).normal().unit(), shape0, leftOverTime));
                    }
                    else {
                        shape0.triggerCollisionEvent(new ShapeCollision(averagePoint, collisionAxis, shape1, leftOverTime));
                        shape1.triggerCollisionEvent(new ShapeCollision(averagePoint, collisionAxis, shape0, leftOverTime));
                    }

                    return true;
                }
            }

            // POLYGON-CIRCLE collision
            else {
                // Get relative position of circle
                Vector2 circleLocal = shape1.position.sum(shape0.position.product(-1)).toLocalSpace(relVelocity);
                double circleRadius = shape1.size.y / 2;

                // No collision if extrema along the line of displacement are farther apart than maxDisplacement
                if (circleLocal.x - circleRadius - shape0Ex[2].local.x > maxDisplacement) {
                    return false;
                }

                // No collision if shapes don't overlap along the local Y axis
                if (shape0Ex[0].local.y >= circleLocal.y + circleRadius || shape0Ex[1].local.y <= circleLocal.y - circleRadius) {
                    return false;
                }

                // Get minimum overlapping point
                SweepPoint point = shape0Ex[0];
                while (point != null && point.local.y <= circleLocal.y) {
                    point = point.right();
                }

                // Offset by -1 so all overlapping edges are checked
                if (point != null && point.left() != null) {
                    point = point.left();
                }

                // Get points of contact
                Vector2 contactPoint = null;
                Vector2 collisionAxis = relVelocity.normal().unit();
                double minDistance = maxDisplacement;

                if (point != null) {
                    while (point != null && point.local.y < circleLocal.y + circleRadius) {
                        // Test point-circle contact
                        double cY = Math.abs((point.local.y - circleLocal.y) / circleRadius);
                        if (cY < 1) {
                            double cX = Math.cos(cY * Math.PI / 2) * circleRadius;
                            double distance = circleLocal.x - cX - point.local.x;
                            if (distance < minDistance) {
                                minDistance = distance;
                                double velocityAlpha = minDistance / maxDisplacement;
                                contactPoint = point.world.sum(shape0.velocity.product(deltaTime * velocityAlpha));
                                collisionAxis = null;
                            }
                        }

                        // Test edge-circle contact
                        SweepPoint edgeTarget = point.right();
                        if (edgeTarget != null) {
                            Vector2 edgeDir = edgeTarget.local.sum(point.local.product(-1)).unit();
                            Vector2 cPoint = circleLocal.sum(edgeDir.normal().product(circleRadius));
                            if (cPoint.y >= point.local.y && cPoint.y <= edgeTarget.local.y) {
                                double edgeAlpha = (cPoint.y - point.local.y) / (edgeTarget.local.y - point.local.y);
                                double distance = cPoint.x - (point.local.x * (1 - edgeAlpha) + edgeTarget.local.x * edgeAlpha);
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    Vector2 worldPoint = point.world.product(1 - edgeAlpha).sum(edgeTarget.world.product(edgeAlpha));
                                    double velocityAlpha = minDistance / maxDisplacement;
                                    contactPoint = worldPoint.sum(shape0.velocity.product(deltaTime * velocityAlpha));
                                    collisionAxis = edgeTarget.world.sum(point.world.product(-1)).unit();
                                }
                            }
                        }

                        // Test next point
                        point = edgeTarget;
                    }
                }

                // Trigger collision if contact points exist
                if (contactPoint != null) {
                    // Update shapes
                    double velocityAlpha = minDistance / maxDisplacement;
                    double leftOverTime = (1 - velocityAlpha) * deltaTime;

                    shape0.moveTo(shape0.position.sum(shape0.velocity.product(deltaTime * velocityAlpha)).sum(shape0.velocity.unit().product(-.1)));
                    shape1.moveTo(shape1.position.sum(shape1.velocity.product(deltaTime * velocityAlpha)).sum(shape1.velocity.unit().product(-.1)));

                    // Trigger collision events
                    if (collisionAxis == null) {
                        shape0.triggerCollisionEvent(new ShapeCollision(contactPoint, contactPoint.sum(shape0.position.product(-1)).normal().unit(), shape1, leftOverTime));
                        shape1.triggerCollisionEvent(new ShapeCollision(contactPoint, contactPoint.sum(shape1.position.product(-1)).normal().unit(), shape0, leftOverTime));
                    }
                    else {
                        shape0.triggerCollisionEvent(new ShapeCollision(contactPoint, collisionAxis, shape1, leftOverTime));
                        shape1.triggerCollisionEvent(new ShapeCollision(contactPoint, collisionAxis, shape0, leftOverTime));
                    }
                    return true;
                }
            }
        }

        // CIRCLE-CIRCLE collision
        else {

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
