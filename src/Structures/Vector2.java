/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/Vector2.java
 */


package Structures;

import java.util.Objects;

public class Vector2 {
    public static final Vector2 ZERO = new Vector2(0, 0);
    public final double x, y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 sum(Vector2 v) {
        return new Vector2(this.x + v.x, this.y + v.y);
    }

    public Vector2 sum(double x, double y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 product(double scale) {
        return new Vector2(this.x * scale, this.y * scale);
    }

    public Vector2 normal() {
        double length = this.magnitude();
        return new Vector2(this.x / length, this.y / length);
    }

    public double magnitude() {
        return Math.pow(Math.pow(this.x, 2) + Math.pow(this.y, 2), 0.5);
    }

    public double dot(Vector2 v) {
        return this.x * v.x + this.y * v.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector2 vector2 = (Vector2) o;
        return Double.compare(vector2.x, x) == 0 && Double.compare(vector2.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}