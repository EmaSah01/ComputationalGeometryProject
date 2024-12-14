package convex_hull;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Stack;

public class GrahamScan {

    public static ArrayList<Point> findConvexHull(ArrayList<Point> points) {
        if (points == null || points.size() < 3) {
            throw new IllegalArgumentException("Convex hull requires at least 3 points.");
        }

        points.sort(Comparator.comparingInt(p -> p.x));

        System.out.println("Sorted points: ");
        for (Point p : points) {
            System.out.println(p);
        }

        Stack<Point> upper = new Stack<>();
        Stack<Point> lower = new Stack<>();

        for (Point p : points) {
            while (upper.size() >= 2 && orientation(upper.get(upper.size() - 2), upper.peek(), p) <= 0) {
                System.out.println("Removing point from upper envelope: " + upper.pop());
            }
            upper.push(p);
            System.out.println("Added point to upper envelope: " + p);
        }

        for (int i = points.size() - 1; i >= 0; i--) {
            Point p = points.get(i);
            while (lower.size() >= 2 && orientation(lower.get(lower.size() - 2), lower.peek(), p) <= 0) {
                System.out.println("Removing point from lower envelope: " + lower.pop());
            }
            lower.push(p);
            System.out.println("Added point to lower envelope: " + p);
        }

        ArrayList<Point> convexHull = new ArrayList<>(upper);

        lower.pop();
        convexHull.addAll(lower);

        System.out.println("Final Convex Hull: ");
        for (Point p : convexHull) {
            System.out.println(p);
        }

        return convexHull;
    }

    private static int orientation(Point p, Point q, Point r) {
        int determinant = (q.x - p.x) * (r.y - q.y) - (q.y - p.y) * (r.x - q.x);
        if (determinant > 0) return 1; 
        if (determinant < 0) return -1; 
        return 0; 
    }
}
