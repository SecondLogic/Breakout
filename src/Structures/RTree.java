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

public class RTree <BoundedObject extends BoundingBox> {
    private static final int DEFAULT_BRANCH_MAX = 3;
    private static final int DEFAULT_LEAF_MAX = 4;

    private RTreeNode root;
    private int branchMax, leafMax, height;

    public RTree() {
        this(DEFAULT_BRANCH_MAX, DEFAULT_LEAF_MAX);
    }

    public RTree(int branchMax, int leafMax) {
        this.branchMax = Math.max(3, branchMax);
        this.leafMax = Math.max(3, leafMax);

        this.root = new RTreeNode<BoundedObject>(true);
        this.height = 1;
    }

    private static RTreeNode getMinArea(RTreeNode[] children, BoundingBox shape) {
        double minArea = Double.MAX_VALUE;
        RTreeNode bestBranch = null;

        for (RTreeNode currentBranch : children) {
            // Get area
            double area = currentBranch.area();

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

    private static RTreeNode getMinAreaChange(RTreeNode[] children, BoundingBox shape) {
        double minAreaChange = Double.MAX_VALUE;
        RTreeNode bestBranch = null;

        for (RTreeNode currentBranch : children) {
            // Get area change
            double areaChange = currentBranch.area() - currentBranch.expand(shape).area();

            // Set minimum
            if (bestBranch == null || areaChange < minAreaChange) {
                minAreaChange = areaChange;
                bestBranch = currentBranch;
            }

            // Resolve ties with smaller initial area
            else {
                RTreeNode[] compareGroup = {bestBranch, currentBranch};
                bestBranch = getMinArea(compareGroup, shape);
            }
        }

        return bestBranch;
    }

    private static RTreeNode getMinOverlapChange(RTreeNode[] children, BoundingBox shape) {
        double minOverlapChange = Double.MAX_VALUE;
        RTreeNode bestBranch = null;

        for (RTreeNode currentBranch : children) {
            // Get overlap area
            double oldOverlapArea = 0;
            for (RTreeNode overlappingRegion : children) {
                if (overlappingRegion != currentBranch) {
                    oldOverlapArea += Math.max(0, currentBranch.getOverlapRegion(overlappingRegion).area());
                }
            }

            BoundingBox proposedRegion = currentBranch.expand(currentBranch);
            double newOverlapArea = 0;
            for (RTreeNode overlappingRegion : children) {
                if (overlappingRegion != currentBranch) {
                    newOverlapArea += Math.max(0, proposedRegion.getOverlapRegion(overlappingRegion).area());
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
                bestBranch = getMinAreaChange(compareGroup, shape);
            }
        }

        return bestBranch;
    }

    private void splitNode(RTreeNode node, boolean reinsert) {
        // Cannot split if node has less than 4 children (branch must have at least 2 children)
        double childCount = node.size();
        assert (childCount >= 4);

        // Create axis comparators
        ArrayList<Comparator<BoundingBox>> axisComparators = new ArrayList<>();
        axisComparators.add(Comparator.comparingDouble(box -> box.min.x));
        axisComparators.add(Comparator.comparingDouble(box -> box.max.x));
        axisComparators.add(Comparator.comparingDouble(box -> box.min.y));
        axisComparators.add(Comparator.comparingDouble(box -> box.max.y));

        // Get split axis with minimum margin
        double minMargin = Double.MAX_VALUE;
        BoundingBox[] bestSplitAxis = null;

        for (Comparator<BoundingBox> axisComparator : axisComparators) {
            // Create split axis
            BoundingBox[] splitAxis = node.getChildren();
            Arrays.sort(splitAxis, axisComparator);

            // Get split axis margin
            double splitMargin = 0;
            for (int splitIndex = 2;  splitIndex <= childCount - 2; splitIndex++) {
                // Add first region margin
                BoundingBox splitRegion0 = splitAxis[0];
                for (int regionIndex = 1; regionIndex < splitIndex; regionIndex++) {
                    splitRegion0 = splitRegion0.expand(splitAxis[regionIndex]);
                }
                splitMargin += splitRegion0.perimeter();

                // Add second region margin
                BoundingBox splitRegion1 = splitAxis[0];
                for (int regionIndex = splitIndex + 1; regionIndex < childCount; regionIndex++) {
                    splitRegion1 = splitRegion1.expand(splitAxis[regionIndex]);
                }
                splitMargin += splitRegion1.perimeter();
            }

            // Set axis with minimum margin
            if (bestSplitAxis == null || splitMargin < minMargin) {
                bestSplitAxis = splitAxis;
                minMargin = splitMargin;
            }
        }

        // Chose split index with minimum overlap
        double minOverlap = Double.MAX_VALUE;
        double bestSplitArea = Double.MAX_VALUE;
        int bestSplitIndex = 0;

        for (int splitIndex = 2;  splitIndex <= childCount - 2; splitIndex++) {
            // Get first region
            BoundingBox splitRegion0 = bestSplitAxis[0];
            for (int regionIndex = 1; regionIndex < splitIndex; regionIndex++) {
                splitRegion0 = splitRegion0.expand(bestSplitAxis[regionIndex]);
            }

            // Get second region
            BoundingBox splitRegion1 = bestSplitAxis[0];
            for (int regionIndex = splitIndex + 1; regionIndex < childCount; regionIndex++) {
                splitRegion1 = splitRegion1.expand(bestSplitAxis[regionIndex]);
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

        if (reinsert) {
            ArrayList<BoundingBox> reinsertList = new ArrayList<>();
            for (int splitIndex = bestSplitIndex; splitIndex < childCount; splitIndex++) {
                node.remove(bestSplitAxis[splitIndex]);
                reinsertList.add(bestSplitAxis[splitIndex]);
            }
            for (BoundingBox reinsertElement : reinsertList) {
                this.insert(this.root, reinsertElement, this.height, 1, false);
            }
        }

        // Split node
        else {
            // Get Parent
            RTreeNode parent = node.getParent();
            if (parent == null) {
                parent = new RTreeNode(false);
                node.setParent(parent);
                parent.insert(node);
                if (node == this.root) {
                    this.root = parent;
                    this.height += 1;
                }
            }

            RTreeNode splitNode = new RTreeNode(parent, node.isLeaf);

            for (int splitIndex = bestSplitIndex; splitIndex < childCount; splitIndex++) {
                node.remove(bestSplitAxis[splitIndex]);
                splitNode.insert(bestSplitAxis[splitIndex]);
            }

            // Split parent if overflow
            if (parent.size() > this.branchMax) {
                splitNode(parent, false);
            }
        }
    }

    private void insert(RTreeNode node, BoundingBox shape, int targetLevel, int currentLevel, boolean reinsert) {
        // Insert if leaf
        if (currentLevel == targetLevel) {
            // Insert shape into leaf node
            node.insert(shape);

            // Split node if overflow
            int nodeMax = this.branchMax;
            if (node.isLeaf) {
                nodeMax = this.leafMax;
            }
            if (node.size() > nodeMax) {
                splitNode(node, reinsert);
            }
        }

        // Find best branch
        else {
            RTreeNode[] children = Arrays.copyOf(node.getChildren(), node.size(), RTreeNode[].class);

            // Chose minimum overlap
            if (children[0].isLeaf) {
                this.insert(getMinOverlapChange(children, shape), shape, targetLevel, currentLevel + 1, reinsert);
            }

            // Chose minimum area
            else {
                for (RTreeNode child : children) {
                    this.insert(getMinAreaChange(children, shape), shape, targetLevel, currentLevel + 1, reinsert);
                }
            }
        }
    }

    public void insert(BoundedObject shape) {
        insert(this.root, shape, this.height, 1, true);
    }

    private boolean remove(RTreeNode node, BoundingBox shape, int currentLevel, int bottomOffset) {
        if (currentLevel == this.height - bottomOffset) {
            if (node.remove(shape)) {
                int nodeMax = this.branchMax;
                int nodeSize = node.size();
                if (node.isLeaf) {
                    nodeMax = this.leafMax;
                }
                /*
                if (nodeSize < nodeMax / 2) {
                    RTreeNode parent = node.getParent();
                    if (node != this.root) {
                        remove(parent, node, currentLevel - 1, bottomOffset + 1);
                        for (BoundingBox child : node.getChildren()) {
                            this.insert(this.root, child, this.height - bottomOffset, 1, false);
                        }
                    }
                    else if (nodeSize == 1 && !node.isLeaf) {
                        this.root = (RTreeNode) node.getChildren()[0];
                        this.root.setParent(null);
                        this.height -= 1;
                    }

                }
                return true;

                 */
            }
        }
        else if (node.encloses(shape)) {
            currentLevel += 1;
            for (BoundingBox child : node.getChildren()) {
                if (remove((RTreeNode) child, shape, currentLevel, bottomOffset)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void remove(BoundingBox shape) {
        this.remove(this.root, shape, 1, 0);
    }

    private ArrayList<BoundedObject> getOverlapping(BoundingBox region, RTreeNode node) {
        ArrayList<BoundedObject> overlappingShapes = new ArrayList<>();
        if (node.isLeaf) {
            for (BoundingBox child : node.getChildren()) {
                if (region.overlaps(child)) {
                    overlappingShapes.add((BoundedObject) child);
                }
            }
        }
        else if (region.encloses(node)) {
            overlappingShapes = ((RTreeNode) node).getLeafChildren();
        }
        else if (region.overlaps(node)) {
            for (RTreeNode child : Arrays.copyOf(node.getChildren(), node.size(), RTreeNode[].class)) {
                overlappingShapes.addAll(getOverlapping(region, child));
            }
        }

        return overlappingShapes;
    }

    public ArrayList<BoundedObject> getOverlapping(BoundingBox region) {
        return this.getOverlapping(region, this.root);
    }

    public boolean isEmpty() {
        return this.root.isEmpty();
    }

    public ArrayList<BoundedObject> getLeafChildren() {
        return this.root.getLeafChildren();
    }
}
