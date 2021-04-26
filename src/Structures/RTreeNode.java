/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/RTreeNode.java
 */

package Structures;

import java.util.ArrayList;

public class RTreeNode <DataObject extends BoundedObject> implements BoundedObject {
    private BoundingBox bounds;
    private ArrayList<RTreeNode> children;
    public final DataObject data;

    public RTreeNode() {
        this.children = new ArrayList<RTreeNode>();
        this.data = null;
        this.bounds = new BoundingBox();
    }

    public RTreeNode(DataObject data) {
        this.children = new ArrayList<RTreeNode>();
        this.data = data;
        this.bounds = data.getBounds();
    }

    private void updateBounds() {
        if (this.children.isEmpty()) {
            this.bounds = new BoundingBox();
        }
        else {
            BoundingBox newBounds = this.children.get(0).getBounds();
            for (RTreeNode child : children) {
                newBounds = newBounds.expand(child.getBounds());
            }
            this.bounds = newBounds;
        }
    }

    public boolean insert(RTreeNode entry) {
        if (this.data == null && !children.contains(entry)) {
            this.children.add(entry);
            this.updateBounds();
            return true;
        }
        return false;
    }

    public boolean remove(RTreeNode entry) {
        if (this.data == null && children.contains(entry)) {
            this.children.remove(entry);
            this.updateBounds();
            return true;
        }
        return false;
    }

    public int size() {
        return this.children.size();
    }

    public int height() {
        if (!this.children.isEmpty()) {
            return 1 + this.children.get(0).height();
        }
        return 0;
    }

    public RTreeNode[] getChildren() {
        return this.children.toArray(new RTreeNode[this.size()]);
    }

    public BoundingBox getBounds() {
        return this.bounds;
    }
}