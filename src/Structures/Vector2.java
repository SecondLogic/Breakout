/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/Vector2.java

    VISUALIZATION OF TRANSFORMS:
        https://www.desmos.com/calculator/mmc3t9hnil
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

    public static Vector2 rotationToVector(double rotation) {
        rotation = Math.toRadians(rotation);
        return new Vector2(Math.cos(rotation), Math.sin(rotation));
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

    public Vector2 product(double scaleX, double scaleY) {
        return new Vector2(this.x * scaleX, this.y * scaleY);
    }

    public Vector2 product(Vector2 scale) {
        return new Vector2(this.x * scale.x, this.y * scale.y);
    }

    public Vector2 unit() {
        if (this.equals(Vector2.ZERO)) {
            return this;
        }
        else {
            return this.product(1 / this.magnitude());
        }
    }

    public Vector2 normal() {
        return new Vector2(-this.y, this.x);
    }

    public double magnitude() {
        return Math.pow(Math.pow(this.x, 2) + Math.pow(this.y, 2), 0.5);
    }

    public double dot(Vector2 v) {
        return this.x * v.x + this.y * v.y;
    }

    public Vector2 toLocalSpace(Vector2 v) {
        v = v.unit();
        return new Vector2(this.dot(v), this.dot(v.normal()));
    }

    public Vector2 toWorldSpace(Vector2 v) {
        return this.toLocalSpace(v.product(1, -1));
    }

    public Vector2 rotate(double rotation) {
        return this.toWorldSpace(Vector2.rotationToVector(rotation));
    }

    public Vector2 reflect(Vector2 v) {
        v = v.unit();
        return this.sum(v.product(-2 * this.dot(v)));
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