package voronoi;

import java.awt.*;
import java.util.*;
import java.util.List;
import triangulation.DelaunayTriangulation;

public class VoronoiDiagram {
    private DelaunayTriangulation delaunay;
    private Set<Point> voronoiPoints;
    private List<LineSegment> voronoiEdges;

    public VoronoiDiagram(DelaunayTriangulation delaunay) {
        this.delaunay = delaunay;
        this.voronoiPoints = new HashSet<>();
        this.voronoiEdges = new ArrayList<>();
        computeVoronoi();
    }

    private void computeVoronoi() {
        Set<DelaunayTriangulation.Edge> delaunayEdges = delaunay.getEdges();
        
        // Iterate through each Delaunay edge
        for (DelaunayTriangulation.Edge edge : delaunayEdges) {
            Point p1 = edge.p1;
            Point p2 = edge.p2;
            
            // Find the third point that forms the triangle with the edge
            Point thirdPoint = findThirdPointForEdge(p1, p2);
            
            if (thirdPoint != null) {
                // Calculate the circumcenter using the edge and the third point
                Point circumcenter = findCircumcenter(p1, p2, thirdPoint);
                
                if (circumcenter != null) {
                    voronoiPoints.add(circumcenter);
                    LineSegment edgeSegment = new LineSegment(edge.p1, circumcenter);
                    voronoiEdges.add(edgeSegment);
                }
            }
        }
    }

    // Find the third point for the triangle formed by the edge p1-p2
    private Point findThirdPointForEdge(Point p1, Point p2) {
        for (DelaunayTriangulation.Edge otherEdge : delaunay.getEdges()) {
            if ((otherEdge.p1.equals(p1) || otherEdge.p2.equals(p1)) &&
                (otherEdge.p1.equals(p2) || otherEdge.p2.equals(p2))) {
                // Find the third point that is common in the other edge
                if (!otherEdge.p1.equals(p1) && !otherEdge.p1.equals(p2)) {
                    return otherEdge.p1;
                }
                if (!otherEdge.p2.equals(p1) && !otherEdge.p2.equals(p2)) {
                    return otherEdge.p2;
                }
            }
        }
        return null;  // Return null if no third point is found
    }

    // Calculate the circumcenter for three points
    private Point findCircumcenter(Point a, Point b, Point c) {
        double d = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
        if (d == 0) return null;

        double ux = ((a.x * a.x + a.y * a.y) * (b.y - c.y)
                     + (b.x * b.x + b.y * b.y) * (c.y - a.y)
                     + (c.x * c.x + c.y * c.y) * (a.y - b.y)) / d;

        double uy = ((a.x * a.x + a.y * a.y) * (c.x - b.x)
                     + (b.x * b.x + b.y * b.y) * (a.x - c.x)
                     + (c.x * c.x + c.y * c.y) * (b.x - a.x)) / d;

        return new Point((int) Math.round(ux), (int) Math.round(uy));
    }

    // Visualize the Voronoi diagram
    public void visualize(Graphics g) {
        for (LineSegment segment : voronoiEdges) {
            g.setColor(Color.RED);
            g.drawLine(segment.p1.x, segment.p1.y, segment.p2.x, segment.p2.y);
        }

        for (Point point : voronoiPoints) {
            g.setColor(Color.BLUE);
            g.fillOval(point.x - 3, point.y - 3, 6, 6);
        }
    }

    // Line segment class to connect two points
    public static class LineSegment {
        public Point p1, p2;

        public LineSegment(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
}

