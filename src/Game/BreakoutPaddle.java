/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/05/2021

    Breakout
    Game/BreakoutPaddle.java
 */

package Game;

import Simulation.SimulatedRectangle;
import Structures.Vector2;

public class BreakoutPaddle extends SimulatedRectangle {
    private BreakoutRoom room;
    public BreakoutPaddle(Vector2 size, Vector2 position, BreakoutRoom room) {
        super(size, position);
        this.room = room;
    }
}
