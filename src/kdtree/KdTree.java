package kdtree;

import java.awt.Point;
import java.util.ArrayList;

public class KdTree {
    public static class Node {
        public Point point;  
        public Node left;   
        public Node right;   
        int depth;          

        public Node(Point point, int depth) {
            this.point = point;
            this.depth = depth;
            this.left = null;
            this.right = null;
        }
    }

    public Node root;

    public KdTree(ArrayList<Point> points) {
        this.root = buildKDTree(points, 0);
    }

    private Node buildKDTree(ArrayList<Point> points, int depth) {
        if (points.isEmpty()) {
            System.out.println("No points left to process. Returning null.");
            return null;
        }

        int axis = depth % 2; 
        System.out.println("Depth: " + depth + " Axis: " + (axis == 0 ? "x" : "y"));

        points.sort((p1, p2) -> axis == 0 ? Integer.compare(p1.x, p2.x) : Integer.compare(p1.y, p2.y));
        System.out.println("Sorted points: " + points);

        int medianIndex = points.size() / 2;
        Point medianPoint = points.get(medianIndex);
        System.out.println("Median point at depth " + depth + ": " + medianPoint);

        Node node = new Node(medianPoint, depth);

        ArrayList<Point> leftPoints = new ArrayList<>(points.subList(0, medianIndex)); 
        ArrayList<Point> rightPoints = new ArrayList<>(points.subList(medianIndex + 1, points.size())); 

        System.out.println("Left points: " + leftPoints);
        System.out.println("Right points: " + rightPoints);

        node.left = buildKDTree(leftPoints, depth + 1);  
        node.right = buildKDTree(rightPoints, depth + 1); 

        return node;  
    }

}

