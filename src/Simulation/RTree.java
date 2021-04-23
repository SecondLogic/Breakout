/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/RTree.java
 */

package Simulation;

import java.util.ArrayList;

public class RTree {
    protected RTreeNode root;
    private int bMax, lMax;

    public RTree(int bMax, int lMax) {
        this.bMax = bMax;
        this.lMax = lMax;

        this.root = new RTreeNode(lMax);
    }

    protected void insert(SimulatedShape shape) {
        // RTREE INSERT
    }

    protected void remove(SimulatedShape shape) {
        // RTREE REMOVE
    }

    private static ArrayList<SimulatedShape> getOverlapping(BoundingBox region, BoundingBox node) {
        ArrayList<SimulatedShape> overlappingShapes = new ArrayList<>();
        if (node instanceof SimulatedShape) {
            if (region.overlaps(node)) {
                overlappingShapes.add((SimulatedShape) node);
            }
        }
        else if (node instanceof RTreeNode) {
            if (region.encloses(node)) {
                overlappingShapes = ((RTreeNode) node).getShapes();
            }
            else if (node.overlaps(region)) {
                for (BoundingBox child : ((RTreeNode) node).getChildren()) {
                    overlappingShapes.addAll(getOverlapping(region, child));
                }
            }
        }

        return overlappingShapes;
    }

    public ArrayList<SimulatedShape> getOverlapping(BoundingBox region) {
        return getOverlapping(region, this.root);
    }

    public boolean isEmpty() {
        return this.root.isEmpty();
    }
}
