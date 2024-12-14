package triangulation;

import convex_hull.GiftWrapping;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

public class Triangulation {

    public static class Edge {
        public Point start;
        public Point end;

        public Edge(Point start, Point end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "Edge: (" + start.x + ", " + start.y + ") -> (" + end.x + ", " + end.y + ")";
        }

        public Point getStart() {
            return start;
        }

        public Point getEnd() {
            return end;
        }
    }

    private ArrayList<Point> points;
    private ArrayList<Point> convexHull;
    private ArrayList<Edge> edges;

    public Triangulation(ArrayList<Point> points) {
        this.points = points;
        this.convexHull = new ArrayList<>();
        this.edges = new ArrayList<>();
    }

    public ArrayList<Edge> triangulate() {
        // Step 1: Compute the convex hull
        convexHull = GiftWrapping.findConvexHull(points);
        System.out.println("Convex Hull: " + convexHull);

        // Step 2: Return early if there are fewer than 3 points
        if (convexHull.size() < 3) return edges;

        // Step 3: Sort vertices by polar angle with respect to a reference point
        Point reference = findLowestPoint(convexHull);
        convexHull.sort((p1, p2) -> {
            double angle1 = Math.atan2(p1.y - reference.y, p1.x - reference.x);
            double angle2 = Math.atan2(p2.y - reference.y, p2.x - reference.x);
            return Double.compare(angle1, angle2);
        });

        System.out.println("Sorted Convex Hull: " + convexHull);

        // Step 4: Perform triangulation using a stack
        performSweepLineTriangulation(reference);

        // Print all edges after triangulation
        System.out.println("Triangulation Edges:");
        for (Edge edge : edges) {
            System.out.println(edge);
        }

        return edges;
    }

    private void performSweepLineTriangulation(Point reference) {
        Stack<Point> stack = new Stack<>();

        // Push the first two vertices onto the stack
        stack.push(convexHull.get(0));
        stack.push(convexHull.get(1));

        // Traverse through the sorted convex hull vertices starting from the third vertex
        for (int i = 2; i < convexHull.size(); i++) {
            Point current = convexHull.get(i);

            if (isOnOppositeSide(stack, current)) {
                Point last = null;
                while (stack.size() > 1) {
                    last = stack.pop();
                    edges.add(new Edge(current, last));
                }
                stack.pop();
                if (last != null) stack.push(last);
                stack.push(current);
            } else {
                while (stack.size() > 1) {
                    Point top = stack.pop();
                    edges.add(new Edge(current, top));

                    Point secondTop = stack.peek();
                    if (!isSameSide(reference, secondTop, current)) {
                        stack.push(top);
                        break;
                    }
                }
                edges.add(new Edge(current, stack.peek()));
                stack.push(current);
            }
        }

        // Connect remaining vertices in the stack
        Point last = stack.pop();
        while (!stack.isEmpty()) {
            edges.add(new Edge(last, stack.pop()));
        }
    }

    private boolean isOnOppositeSide(Stack<Point> stack, Point current) {
        if (stack.size() < 2) return false;

        Point top = stack.peek();
        Point secondTop = stack.get(stack.size() - 2);

        int crossProduct = (top.x - secondTop.x) * (current.y - secondTop.y) -
                           (top.y - secondTop.y) * (current.x - secondTop.x);

        return crossProduct < 0;
    }

    private boolean isSameSide(Point origin, Point p1, Point p2) {
        int crossProduct = (p1.x - origin.x) * (p2.y - origin.y) -
                           (p1.y - origin.y) * (p2.x - origin.x);
        return crossProduct >= 0;
    }

    private Point findLowestPoint(ArrayList<Point> points) {
        return points.stream()
                .min(Comparator.comparingInt((Point p) -> p.y)
                        .thenComparingInt(p -> p.x))
                .orElseThrow(() -> new IllegalArgumentException("Point list is empty"));
    }
}
