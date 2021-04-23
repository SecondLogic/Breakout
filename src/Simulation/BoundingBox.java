/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/BoundingBox.java
 */

package Simulation;

import java.util.Objects;

public class BoundingBox {
    protected Vector2 min, max;

    public BoundingBox() {
        this.min = Vector2.ZERO;
        this.max = Vector2.ZERO;
    }

    public BoundingBox(Vector2 min, Vector2 max) {
        this.min = min;
        this.max = max;
    }

    public void setMin(Vector2 min) {
        this.min = min;
    }

    public void setMax(Vector2 max) {
        this.max = max;
    }

    public Vector2 getMin() {
        return this.min;
    }

    public Vector2 getMax() {
        return this.max;
    }

    public boolean overlaps(BoundingBox o) {
        return (o.getMin().x >= this.min.x && o.getMin().y >= this.min.y
                && o.getMin().x <= this.max.x && o.getMin().y <= this.max.y)
                || (o.getMax().x >= this.min.x && o.getMax().y >= this.min.y
                && o.getMax().x <= this.max.x && o.getMax().y <= this.max.y);
    }

    public boolean encloses(BoundingBox o) {
        return (o.getMin().x >= this.min.x && o.getMin().y >= this.min.y
                && o.getMin().x <= this.max.x && o.getMin().y <= this.max.y)
                && (o.getMax().x >= this.min.x && o.getMax().y >= this.min.y
                && o.getMax().x <= this.max.x && o.getMax().y <= this.max.y);
    }
}
