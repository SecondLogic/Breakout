/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/05/2021

    Breakout
    Game/BreakoutBrick.java
 */

package Game;

import Simulation.SimulatedRectangle;
import Structures.Vector2;

public class BreakoutBrick extends SimulatedRectangle {
    private BreakoutRoom room;

    public BreakoutBrick(Vector2 size, Vector2 position, BreakoutRoom room) {
        super(size, position);
        this.room = room;

        this.setOnCollide(collision -> {
            if (collision.collidedShape instanceof BreakoutBall) {
                this.room.remove(this);
            }
        });

        this.room.add(this);
    }
}
