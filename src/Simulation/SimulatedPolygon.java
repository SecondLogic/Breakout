/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/29/2021

    Breakout
    Simulation/SimulatedPolygon.java
 */


package Simulation;

import Structures.Vector2;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;

public class SimulatedPolygon extends SimulatedShape {
    private final Vector2[] points;

    public SimulatedPolygon(Vector2 size, Vector2 position, Vector2[] points) {
        super(new Polygon(), size, position);
        assert(points.length >= 3);
        this.points = points;

        Polygon shape = (Polygon) this.uiNode;

        ArrayList<Double> pointList = new ArrayList<>();

        for (Vector2 point : this.points) {
            pointList.add(point.x);
            pointList.add(point.y);
        }

        Platform.runLater(() -> {
            shape.getPoints().addAll(pointList);
            shape.setLayoutX(position.x);
            shape.setLayoutY(position.y);
            shape.setFill(Color.WHITE);
        });
    }

    public SimulatedPolygon(Vector2 size, Vector2 position, Vector2[] points, SimulationSpace space) {
        this(size, position, points);
        space.add(this);
    }

    public Vector2[] getPoints() {
        return this.points;
    }

    @Override
    public void moveTo(Vector2 position) {
        super.moveTo(position);

        Platform.runLater(() -> {
            this.uiNode.setLayoutX(this.position.x);
            this.uiNode.setLayoutY(this.position.y);
        });
    }

    @Override
    public void resize(Vector2 size) {
        super.resize(size);

        Polygon shape = (Polygon) this.uiNode;

        Bounds oldBounds = shape.getBoundsInLocal();
        Vector2 scale = new Vector2(size.x / oldBounds.getWidth(), size.y / oldBounds.getHeight());

        ArrayList<Double> pointList = new ArrayList<>();
        for (int pointIndex = 0; pointIndex < this.points.length; pointIndex++) {
            Vector2 point = this.points[pointIndex].product(scale);
            this.points[pointIndex] = point;

            pointList.add(point.x);
            pointList.add(point.y);
        }

        Platform.runLater(() -> {
            shape.getPoints().setAll(pointList);
            shape.setLayoutX(this.position.x);
            shape.setLayoutY(this.position.y);
        });
    }

    @Override
    public void setRotation(double rotation) {
        super.setRotation(rotation);

        Polygon shape = (Polygon) this.uiNode;

        Platform.runLater(() -> {
            shape.setRotate(rotation);
        });
    }
}