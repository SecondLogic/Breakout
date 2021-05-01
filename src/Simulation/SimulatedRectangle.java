/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedRectangle.java
 */

package Simulation;

import Structures.Vector2;
import javafx.scene.shape.Polygon;

public class SimulatedRectangle extends SimulatedPolygon {
    public SimulatedRectangle(Vector2 size, Vector2 position) {
        super(size, position, new Vector2[] {
            Vector2.ZERO,
            new Vector2(size.x, 0),
            size,
            new Vector2(0, size.y),
        });
    }

    public SimulatedRectangle(Vector2 size, Vector2 position, SimulationSpace space) {
        this(size, position);
        space.add(this);
    }
}