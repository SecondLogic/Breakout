/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/RTreeNode.java
 */

package Simulation;

import java.util.ArrayList;

public class RTreeNode extends BoundingBox{
    private RTreeNode parent;
    private ArrayList<BoundingBox> children;
    private int nodeMax;
    private SimulatedShape shape;

    private void updateBounds() {
        if (this.children.size() == 0) {
            this.min = Vector2.ZERO;
            this.max = Vector2.ZERO;
        }

        double minX = children.get(0).getMin().x;
        double minY = children.get(0).getMin().y;
        double maxX = children.get(0).getMax().x;
        double maxY = children.get(0).getMax().y;

        for (BoundingBox child : children) {
            minX = Math.min(child.getMin().x, minX);
            minY = Math.min(child.getMin().y, minY);
            minX = Math.max(child.getMax().x, maxX);
            minY = Math.max(child.getMax().y, maxY);
        }

        this.min = new Vector2(minX, minY);
        this.max = new Vector2(maxX, maxY);
    }

    public RTreeNode(int nodeMax) {
        this.parent = null;
        this.nodeMax = nodeMax;
        this.children = new ArrayList<BoundingBox>();
    }

    public RTreeNode(RTreeNode parent, int nodeMax) {
        this.parent = parent;
        this.children = new ArrayList<BoundingBox>();
    }

    public RTreeNode getParent() {
        return this.parent;
    }

    public RTreeNode setParent(RTreeNode parent) {
        return this.parent = parent;
    }

    public boolean insert(BoundingBox o) {
        if (this.children.size() >= this.nodeMax) {
            return false;
        }

        this.children.add(o);
        this.updateBounds();

        return true;
    }

    public boolean remove(BoundingBox o) {
        boolean removed = this.children.remove(o);
        if (removed) {
            this.updateBounds();
        }
        return removed && this.children.size() < this.nodeMax / 2;
    }

    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    public ArrayList<SimulatedShape> getShapes() {
        ArrayList<SimulatedShape> shapes = new ArrayList<>();
        for (BoundingBox child : children) {
            if (child instanceof SimulatedShape) {
                shapes.add((SimulatedShape) child);
            }
            else if (child instanceof RTreeNode) {
                shapes.addAll(((RTreeNode) child).getShapes());
            }
        }
        return shapes;
    }

    public BoundingBox[] getChildren() {
        return (BoundingBox[]) this.children.toArray();
    }
}
