/*
    Amos Cabudol
    CSPC-24500, Lewis University
    04/22/2021

    Breakout
    Simulation/RTree.java

    Sources:
    Burroughs, B+Tree Insertions & B+Tree Deletions
        https://www.youtube.com/watch?v=h6Mw7_S4ai0
        https://www.youtube.com/watch?v=QrbaQDSuxIM

    Guttman, R-Trees - A Dynamic Index Structure for Spatial Searching
        http://www-db.deis.unibo.it/courses/SI-LS/papers/Gut84.pdf

    Beckmann et. al., The R*-tree: An Efficient and Robust Access Method for Points and Rectangles
        https://infolab.usc.edu/csci599/Fall2001/paper/rstar-tree.pdf
 */

package Structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class RTree <DataObject extends BoundedObject> {
    // Default node capacity values
    private static final int DEFAULT_BRANCH_MAX = 3;
    private static final int DEFAULT_LEAF_MAX = 4;

    // Split axis comparators
    private static final Comparator[] AXIS_COMPARATORS = {
        Comparator.comparingDouble((RTreeNode bounds) -> bounds.getBounds().min.x),
        Comparator.comparingDouble((RTreeNode bounds) -> bounds.getBounds().max.x),
        Comparator.comparingDouble((RTreeNode bounds) -> bounds.getBounds().min.y),
        Comparator.comparingDouble((RTreeNode bounds) -> bounds.getBounds().max.y)
    };

    private RTreeNode root;
    private int branchMax, leafMax;

    public RTree() {
        this(DEFAULT_BRANCH_MAX, DEFAULT_LEAF_MAX);
    }

    public RTree(int branchMax, int leafMax) {
        this.branchMax = Math.max(3, branchMax);
        this.leafMax = Math.max(3, leafMax);

        this.root = new RTreeNode<DataObject>();
    }

    private static RTreeNode getMinArea(RTreeNode[] children) {
        double minArea = Double.MAX_VALUE;
        RTreeNode bestBranch = null;

        for (RTreeNode currentBranch : children) {
            // Get area
            BoundingBox branchBounds = currentBranch.getBounds();
            double area = branchBounds.area();

            // Set minimum
            if (bestBranch == null || area < minArea) {
                minArea = area;
                bestBranch = currentBranch;
            }

            // Resolve ties with fewer children
            else if (area == minArea && currentBranch.size() < bestBranch.size()) {
                bestBranch = currentBranch;
            }
        }

        return bestBranch;
    }

    private static RTreeNode getMinAreaChange(RTreeNode[] children, BoundingBox bounds) {
        double minAreaChange = Double.MAX_VALUE;
        RTreeNode bestBranch = null;

        for (RTreeNode currentBranch : children) {
            // Get area change
            BoundingBox branchBounds = currentBranch.getBounds();
            double areaChange = branchBounds.area() - branchBounds.expand(bounds).area();

            // Set minimum
            if (bestBranch == null || areaChange < minAreaChange) {
                minAreaChange = areaChange;
                bestBranch = currentBranch;
            }

            // Resolve ties with smaller initial area
            else {
                RTreeNode[] compareGroup = {bestBranch, currentBranch};
                bestBranch = getMinArea(compareGroup);
            }
        }

        return bestBranch;
    }

    private static RTreeNode getMinOverlapChange(RTreeNode[] children, BoundingBox bounds) {
        double minOverlapChange = Double.MAX_VALUE;
        RTreeNode bestBranch = null;

        for (RTreeNode currentBranch : children) {
            BoundingBox branchBounds = currentBranch.getBounds();

            // Get change in overlap area
            double oldOverlapArea = 0;
            for (RTreeNode overlappingRegion : children) {
                BoundingBox overlappingBounds = overlappingRegion.getBounds();
                if (overlappingBounds != branchBounds) {
                    oldOverlapArea += Math.max(0, branchBounds.getOverlapRegion(overlappingBounds).area());
                }
            }

            BoundingBox proposedRegion = branchBounds.expand(bounds);
            double newOverlapArea = 0;
            for (RTreeNode overlappingRegion : children) {
                BoundingBox overlappingBounds = overlappingRegion.getBounds();
                if (overlappingBounds != branchBounds) {
                    oldOverlapArea += Math.max(0, proposedRegion.getOverlapRegion(overlappingBounds).area());
                }
            }

            double overlapChange = newOverlapArea - oldOverlapArea;

            // Set minimum
            if (bestBranch == null || overlapChange < minOverlapChange) {
                minOverlapChange = overlapChange;
                bestBranch = currentBranch;
            }

            // Resolve ties with smaller area change
            else if (overlapChange == minOverlapChange) {
                RTreeNode[] compareGroup = {bestBranch, currentBranch};
                bestBranch = getMinAreaChange(compareGroup, bounds);
            }
        }

        return bestBranch;
    }

    private static RTreeNode[] getBestSplitAxis(RTreeNode node) {
        // Find split axis with smallest total margin (sum of perimeters)
        double minMargin = Double.MAX_VALUE;
        RTreeNode[] bestSplitAxis = null;

        for (Comparator<RTreeNode> axisComparator : AXIS_COMPARATORS) {
            // Create split axis
            RTreeNode[] splitAxis = node.getChildren();
            Arrays.sort(splitAxis, axisComparator);

            // Get split axis margin
            double splitMargin = 0;
            for (int splitIndex = 2;  splitIndex <= splitAxis.length - 2; splitIndex++) {
                // Add first region margin
                BoundingBox splitRegion0 = splitAxis[0].getBounds();
                for (int regionIndex = 1; regionIndex < splitIndex; regionIndex++) {
                    splitRegion0 = splitRegion0.expand(splitAxis[regionIndex].getBounds());
                }
                splitMargin += splitRegion0.perimeter();

                // Add second region margin
                BoundingBox splitRegion1 = splitAxis[0].getBounds();
                for (int regionIndex = splitIndex + 1; regionIndex < splitAxis.length; regionIndex++) {
                    splitRegion1 = splitRegion1.expand(splitAxis[regionIndex].getBounds());
                }
                splitMargin += splitRegion1.perimeter();
            }

            // Set axis with minimum margin
            if (bestSplitAxis == null || splitMargin < minMargin) {
                bestSplitAxis = splitAxis;
                minMargin = splitMargin;
            }
        }

        return bestSplitAxis;
    }

    private int getBestSplitIndex(RTreeNode[] splitAxis) {
        // Find best split index based on minimum overlap between split regions
        double minOverlap = Double.MAX_VALUE;
        double bestSplitArea = Double.MAX_VALUE;
        int bestSplitIndex = 0;

        for (int splitIndex = 2;  splitIndex <= splitAxis.length - 2; splitIndex++) {
            // Get first region
            BoundingBox splitRegion0 = splitAxis[0].getBounds();
            for (int regionIndex = 1; regionIndex < splitIndex; regionIndex++) {
                splitRegion0 = splitRegion0.expand(splitAxis[regionIndex].getBounds());
            }

            // Get second region
            BoundingBox splitRegion1 = splitAxis[0].getBounds();
            for (int regionIndex = splitIndex + 1; regionIndex < splitAxis.length; regionIndex++) {
                splitRegion1 = splitRegion1.expand(splitAxis[regionIndex].getBounds());
            }

            // Get overlap
            double splitOverlap = splitRegion0.getOverlapRegion(splitRegion1).area();
            double splitArea = splitRegion0.area() + splitRegion1.area();

            // Set minimum overlap, Resolve tie with smaller total area
            if (bestSplitIndex == 0 || splitOverlap < minOverlap || (splitOverlap == minOverlap && splitArea < bestSplitArea)) {
                minOverlap = splitOverlap;
                bestSplitIndex = splitIndex;
                bestSplitArea = splitArea;
            }
        }

        return bestSplitIndex;
    }

    private void splitNode(RTreeNode node, boolean reinsert) {
        // Cannot split if node has less than 4 children (branch must have at least 2 children)
        double childCount = node.size();
        assert (childCount >= 4);

        // Get split axis with minimum margin
        RTreeNode[] bestSplitAxis = getBestSplitAxis(node);

        // Reinsert split elements on first split
        if (reinsert) {
            ArrayList<RTreeNode> reinsertList = new ArrayList<>();
            for (int splitIndex = getBestSplitIndex(bestSplitAxis); splitIndex < childCount; splitIndex++) {
                node.remove(bestSplitAxis[splitIndex]);
                reinsertList.add(bestSplitAxis[splitIndex]);
            }
            for (RTreeNode reinsertElement : reinsertList) {
                this.insert(this.root, reinsertElement, false);
            }
        }

        // Split node on subsequent splits
        else {
            // Get Parent
            RTreeNode parent = node.getParent();
            if (node == this.root) {
                parent = new RTreeNode<DataObject>();
                parent.insert(node);
                this.root = parent;
            }

            // Create new split node
            RTreeNode<DataObject> splitNode = new RTreeNode();
            parent.insert(splitNode);

            // Distribute elements between the two nodes
            for (int splitIndex = getBestSplitIndex(bestSplitAxis); splitIndex < childCount; splitIndex++) {
                node.remove(bestSplitAxis[splitIndex]);
                splitNode.insert(bestSplitAxis[splitIndex]);
            }

            // Split parent if overflow
            if (parent.size() > this.branchMax) {
                splitNode(parent, false);
            }
        }
    }

    private boolean insert(RTreeNode ancestor, RTreeNode node, boolean reinsert) {
        int ancestorHeight = ancestor.height();
        int nodeHeight = node.height();

        // Insert node at appropriate level
        if (ancestorHeight == 0 || ancestorHeight == nodeHeight + 1) {
            if (ancestor.insert(node)) {
                // Split node if overflow
                int maxChildren = this.branchMax;
                if (ancestorHeight == 1) {
                    maxChildren = this.leafMax;
                }
                if (ancestor.size() > maxChildren) {
                    splitNode(ancestor, reinsert);
                }

                return true;
            }
            return false;
        }

        // Select best branch to insert node
        else {
            RTreeNode[] children = ancestor.getChildren();
            BoundingBox bounds = node.getBounds();

            // Chose minimum overlap for branches pointing directly to leaf nodes
            if (ancestorHeight == 1) {
                return this.insert(getMinOverlapChange(children, bounds), node, reinsert);
            }

            // Chose minimum area for the rest of the branches
            else {
                return this.insert(getMinAreaChange(children, bounds), node, reinsert);
            }
        }
    }

    public boolean insert(DataObject entry) {
        // Start recursive insert
        if (!this.contains(entry)) {
            this.insert(this.root, new RTreeNode(entry), true);
            return true;
        }
        return false;
    }

    private void remove(RTreeNode node) {
        // Remove node
        RTreeNode parent = node.getParent();
        parent.remove(node);

        // Check if parent has below minimum number of children
        int maxChildren = this.branchMax;
        if (parent.height() == 1) {
            maxChildren = this.leafMax;
        }

        if (parent.size() < maxChildren / 2) {
            // Propagate changes upward
            this.remove(parent);

            // Reinsert orphaned children
            for (RTreeNode child : parent.getChildren()) {
                this.insert(this.root, child, false);
            }
        }
    }

    public boolean remove(DataObject entry) {
        // Start propagating remove
        RTreeNode node = this.getNode(entry, this.root);
        if (node != null) {
            this.remove(node);
            return true;
        }
        return false;
    }

    private RTreeNode getNode(DataObject entry, RTreeNode node) {
        if (node.data == entry) {
            return node;
        }
        if (node.getBounds().encloses(entry.getBounds())) {
            for (RTreeNode child : node.getChildren()) {
                RTreeNode foundNode = getNode(entry, child);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    public boolean contains(DataObject entry) {
        // Start recursive search
        return getNode(entry, this.root) != null;
    }

    public ArrayList<DataObject> getObjectsInBranch(RTreeNode<DataObject> node) {
        ArrayList<DataObject> objects = new ArrayList<>();

        // Leaf node reached
        if (node.data != null) {
            objects.add(node.data);
        }

        // Recurse until leaf node is reached
        else {
            for (RTreeNode child : node.getChildren()) {
                objects.addAll(getObjectsInBranch(child));
            }
        }
        return objects;
    }

    public ArrayList<DataObject> getObjectsInRegion(BoundingBox region, RTreeNode<DataObject> node) {
        ArrayList<DataObject> overlappingObjects = new ArrayList<>();

        // Add all leaf descendants if completely enclosed
        if (region.encloses(node.getBounds())) {
            overlappingObjects = getObjectsInBranch(node);
        }

        // Add overlapping descendants
        else if (region.overlaps(node.getBounds())) {
            // Leaf node reached
            if (node.height() == 0) {
                overlappingObjects.add(node.data);
            }

            // Get overlapping descendants
            else {
                for (RTreeNode child : node.getChildren()) {
                    overlappingObjects.addAll(getObjectsInRegion(region, child));
                }
            }
        }

        return overlappingObjects;
    }

    public ArrayList<DataObject> getObjectsInRegion(BoundingBox region) {
        return this.getObjectsInRegion(region, this.root);
    }
}
