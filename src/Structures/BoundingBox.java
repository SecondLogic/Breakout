/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/BoundingBox.java
 */


package Structures;

public class BoundingBox {
    public final Vector2 min, max;

    public BoundingBox() {
        this.min = Vector2.ZERO;
        this.max = Vector2.ZERO;
    }

    public BoundingBox(Vector2 min, Vector2 max) {
        this.min = min;
        this.max = max;
    }

    public boolean encloses(Vector2 v) {
        return v.x >= this.min.x
                && v.y >= this.min.y
                && v.x <= this.max.x
                && v.y <= this.max.y;
    }

    public boolean overlaps(BoundingBox o) {
        return o.max.x >= this.min.x
                && o.max.y >= this.min.y
                && this.max.x >= o.min.x
                && this.max.y >= o.min.y;
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

    public Vector2 position() {
        return new Vector2((this.min.x + this.max.x) / 2, (this.min.y + this.max.y) / 2);
    }

    public BoundingBox expand(Vector2 point) {
        Vector2 eMin = new Vector2(Math.min(this.min.x, point.x), Math.min(this.min.y, point.y));
        Vector2 eMax = new Vector2(Math.max(this.max.x, point.x), Math.max(this.max.y, point.y));
        return new BoundingBox(eMin, eMax);
    }

    public BoundingBox expand(BoundingBox region) {
        Vector2 eMin = new Vector2(Math.min(this.min.x, region.min.x), Math.min(this.min.y, region.min.y));
        Vector2 eMax = new Vector2(Math.max(this.max.x, region.max.x), Math.max(this.max.y, region.max.y));
        return new BoundingBox(eMin, eMax);
    }

    public BoundingBox getOverlapRegion(BoundingBox region) {
        Vector2 oMin = new Vector2(Math.max(this.min.x, region.min.x), Math.max(this.min.y, region.min.y));
        Vector2 oMax = new Vector2(Math.min(this.max.x, region.max.x), Math.min(this.max.y, region.max.y));
        return new BoundingBox(oMin, oMax);
    }

}