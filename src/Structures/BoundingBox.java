/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/BoundingBox.java
 */

package Structures;

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

    public boolean encloses(Vector2 v) {
        return v.x >= this.min.x && v.y >= this.min.y && v.x <= this.max.x && v.y <= this.max.y;
    }

    public boolean overlaps(BoundingBox o) {
        return this.encloses(o.min) || this.encloses(o.max) || this.encloses(new Vector2(o.min.x, o.max.y))
                || this.encloses(new Vector2(o.max.x, o.min.y)) || o.encloses(this.min) || o.encloses(this.max)
                || o.encloses(new Vector2(this.min.x, this.max.y)) || o.encloses(new Vector2(this.max.x, this.min.y));
    }

    public boolean encloses(BoundingBox o) {
        return this.encloses(o.min) && this.encloses(o.max);
    }

    public double area() {
        return this.max.x - this.min.x * this.max.y - this.min.y;
    }

    public double perimeter() {
        return (this.max.x - this.min.x + this.max.y - this.min.y) * 2;
    }

    public BoundingBox expand(Vector2 point) {
        Vector2 eMin = new Vector2(Math.min(this.min.x, point.x), Math.min(this.min.y, point.y));
        Vector2 eMax = new Vector2(Math.max(this.max.x, point.x), Math.min(this.max.y, point.y));
        return new BoundingBox(min, max);
    }

    public BoundingBox expand(BoundingBox region) {
        return this.expand(region.getMax()).expand(region.getMax());
    }

    public BoundingBox getOverlapRegion(BoundingBox region) {
        Vector2 oMin = new Vector2(Math.max(this.min.x, region.getMin().x), Math.max(this.min.y, region.getMin().y));
        Vector2 oMax = new Vector2(Math.min(this.max.x, region.getMax().x), Math.min(this.max.y, region.getMax().y));
        return new BoundingBox(oMin, oMax);
    }

    @Override
    public String toString() {
        return "[" + this.min + ", " + this.max + "]";
    }
}
