/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/RTreeNode.java
 */

package Structures;

import java.util.ArrayList;

public class RTreeNode <BoundedObject extends BoundingBox> extends BoundingBox {
    private RTreeNode parent;
    private ArrayList<BoundingBox> children;
    private BoundedObject shape;
    public final boolean isLeaf;

    public RTreeNode(boolean isLeaf) {
        this(null, isLeaf);
    }

    public RTreeNode(RTreeNode parent, boolean isLeaf) {
        this.parent = parent;
        this.isLeaf = isLeaf;
        this.children = new ArrayList<BoundingBox>();

        if (!(parent == null)) {
            parent.insert(this);
        }
    }

    public RTreeNode getParent() {
        return this.parent;
    }

    public void setParent(RTreeNode parent) {
        this.parent = parent;
    }

    public void updateBounds() {
        if (this.children.size() == 0) {
            this.min = Vector2.ZERO;
            this.max = Vector2.ZERO;
            return;
        }

        double minX = children.get(0).getMin().x;
        double minY = children.get(0).getMin().y;
        double maxX = children.get(0).getMax().x;
        double maxY = children.get(0).getMax().y;

        for (BoundingBox child : children) {
            minX = Math.min(child.getMin().x, minX);
            minY = Math.min(child.getMin().y, minY);
            maxX = Math.max(child.getMax().x, maxX);
            maxY = Math.max(child.getMax().y, maxY);
        }

        this.min = new Vector2(minX, minY);
        this.max = new Vector2(maxX, maxY);

        if (!(this.parent == null)) {
            this.parent.updateBounds();
        }
    }

    public boolean insert(BoundingBox entry) {
        boolean inserted = false;
        if (!this.children.contains(entry)) {
            this.children.add(entry);
            this.updateBounds();
            if (entry instanceof RTreeNode) {
                ((RTreeNode) entry).setParent(this);
            }
            inserted = true;
        }
        return inserted;
    }

    public boolean remove(BoundingBox entry) {
        boolean removed = false;
        if (this.children.remove(entry)) {
            this.updateBounds();
            if (entry instanceof RTreeNode) {
                ((RTreeNode) entry).setParent(null);
            }
            removed = true;
        }
        return removed;
    }

    public boolean isEmpty() {
        return this.children.isEmpty();
    }

    public ArrayList<BoundedObject> getLeafChildren() {
        ArrayList<BoundedObject> shapes = new ArrayList<>();
        for (BoundingBox child : children) {
            if (child instanceof RTreeNode) {
                shapes.addAll(((RTreeNode) child).getLeafChildren());
            }
            else {
                shapes.add((BoundedObject) child);
            }
        }
        return shapes;
    }

    public BoundingBox[] getChildren() {
        return this.children.toArray(new BoundingBox[this.size()]);
    }

    public int size() {
        return this.children.size();
    }
}
