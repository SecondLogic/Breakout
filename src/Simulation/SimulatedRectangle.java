/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/SimulatedRectangle.java
 */

package Simulation;

import Structures.Vector2;

public class SimulatedRectangle extends SimulatedPolygon {
    public SimulatedRectangle(Vector2 size, Vector2 position) {
        super(size, position, new Vector2[] {
            size.product(-.5),
            size.product(.5, -.5),
            size.product(.5),
            size.product(-.5, .5),
        });
    }

    public SimulatedRectangle(Vector2 size, Vector2 position, SimulationSpace space) {
        this(size, position);
        space.add(this);
    }
}