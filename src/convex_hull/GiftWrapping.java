package convex_hull;

import java.awt.Point;
import java.util.ArrayList;

public class GiftWrapping {

    public static ArrayList<Point> findConvexHull(ArrayList<Point> points) {
        if (points.size() < 3) {
            return points;  // there has to be more that 3 points
        }

        ArrayList<Point> hull = new ArrayList<>();

        Point start = findLowestPoint(points);
        Point current = start;
        Point previous = new Point(current.x - 1, current.y);

        hull.add(start);  

        do {
            Point next = null;
            double smallestAngle = Double.MAX_VALUE;

            for (Point candidate : points) {
                if (candidate.equals(current)) {
                    continue;  
                }

                double angle = calculateAngle(previous, current, candidate);

                if (next == null || angle < smallestAngle) {
                    smallestAngle = angle;
                    next = candidate;
                }
            }
            
            previous = current;  
            current = next;
            hull.add(current);  
        } while (!current.equals(start));
		return hull;
    }

    private static Point findLowestPoint(ArrayList<Point> points) {
        Point lowest = points.get(0);

        for (Point p : points) {
            if (p.y < lowest.y) {
                lowest = p;
            }
        }

        return lowest;
    }

    private static double calculateAngle(Point previous, Point current, Point candidate) {
        double deltaX1 = current.x - previous.x;
        double deltaY1 = current.y - previous.y;
        double deltaX2 = candidate.x - current.x;
        double deltaY2 = candidate.y - current.y;

        double dotProduct = deltaX1 * deltaX2 + deltaY1 * deltaY2;
        double magnitude1 = Math.sqrt(deltaX1 * deltaX1 + deltaY1 * deltaY1);
        double magnitude2 = Math.sqrt(deltaX2 * deltaX2 + deltaY2 * deltaY2);

        return Math.acos(dotProduct / (magnitude1 * magnitude2));  
    }
}
