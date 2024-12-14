package triangulation;

import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;


public class DelaunayTriangulation {
    private Set<Edge> AEL = new HashSet<>(); // Active Edge List
    private Set<Edge> DT = new HashSet<>();  // Delaunay Triangulation
    private List<Point> points = new ArrayList<>();
    private List<triangulation.Triangulation.Edge> edges = new ArrayList<>(); // For edges passed during initialization
    private Point p1, p2, p3; // Super-triangle points
    
    public DelaunayTriangulation(List<Point> points) {
        this.points = points;
        initialize();
    }
    
    public DelaunayTriangulation(List<Point> points, ArrayList<triangulation.Triangulation.Edge> triangulationEdges) {
        this.points = points;
        this.edges = triangulationEdges;
        initialize();
    }

    // Initializes with a super-triangle
    private void initialize() {
        int minX = points.stream().mapToInt(p -> p.x).min().orElse(0);
        int minY = points.stream().mapToInt(p -> p.y).min().orElse(0);
        int maxX = points.stream().mapToInt(p -> p.x).max().orElse(1000);
        int maxY = points.stream().mapToInt(p -> p.y).max().orElse(1000);

        p1 = new Point(minX - 10, minY - 10);
        p2 = new Point(maxX + 10, minY - 10);
        p3 = new Point((minX + maxX) / 2, maxY + 20);
        
        System.out.println("Super-triangle points: " + p1 + ", " + p2 + ", " + p3);


        // Add edges of the super-triangle to AEL
        AEL.add(new Edge(p1, p2));
        AEL.add(new Edge(p2, p3));
        AEL.add(new Edge(p3, p1));

        // Add super-triangle vertices to the point list
        points.add(p1);
        points.add(p2);
        points.add(p3);
        
        
        if (edges != null) {
            this.edges.addAll(edges);
        }
    }

    public void performTriangulation() {
        // Main loop to process edges in AEL
        while (!AEL.isEmpty()) {
            Edge currentEdge = AEL.iterator().next();
            AEL.remove(currentEdge);
            System.out.println("Processing edge: " + currentEdge);  // Log the current edge

            Point newPoint = findPointWithMinDelaunayDistance(currentEdge);

            if (newPoint != null) {
                // Create two new edges with the new point
                Edge edge1 = new Edge(currentEdge.p1, newPoint);
                Edge edge2 = new Edge(newPoint, currentEdge.p2);

                addEdgeToAEL(edge1);
                addEdgeToAEL(edge2);

                System.out.println("Created new edges: " + edge1 + ", " + edge2);  // Log new edges
            }

            // Add processed edge to the Delaunay triangulation if it's not a super-triangle edge
            if (!isSuperTriangleEdge(currentEdge)) {
                DT.add(currentEdge);
                System.out.println("Added edge to DT: " + currentEdge);  // Log edge added to DT
            }
        }

        // Remove edges connected to super-triangle vertices
        DT.removeIf(edge -> isSuperTrianglePoint(edge.p1) || isSuperTrianglePoint(edge.p2));
        System.out.println("Removed edges connected to super-triangle points.");
    }

    // Provera da li ivica pripada super-trokutu
    private boolean isSuperTriangleEdge(Edge e) {
        return isSuperTrianglePoint(e.p1) || isSuperTrianglePoint(e.p2);
    }

    // Provera da li tačka pripada super-trokutu
    private boolean isSuperTrianglePoint(Point p) {
        return p.equals(p1) || p.equals(p2) || p.equals(p3);
    }

    private Point findPointWithMinDelaunayDistance(Edge e) {
        Point minPoint = null;
        double minDistance = Double.MAX_VALUE;

        for (Point p : points) {
            if (isLeftOfEdge(e, p)) {
                double dD = computeDelaunayDistance(e, p);
                if (dD < minDistance) {
                    minDistance = dD;
                    minPoint = p;
                }
            }
        }
        return minPoint;
    }

    private double computeDelaunayDistance(Edge e, Point p) {
        // Compute circumcircle radius as the Delaunay distance
        Circle c = circumCircle(e.p1, e.p2, p);
        if (c != null) {
            System.out.println("Circumcircle radius for points " + e.p1 + ", " + e.p2 + ", " + p + ": " + c.radius);
        }
        return (c != null) ? c.radius : Double.MAX_VALUE;
    }

    private Circle circumCircle(Point a, Point b, Point c) {
        double d = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
        if (d == 0) return null; // Collinear points do not form a circumcircle

        double ux = ((a.x * a.x + a.y * a.y) * (b.y - c.y)
                     + (b.x * b.x + b.y * b.y) * (c.y - a.y)
                     + (c.x * c.x + c.y * c.y) * (a.y - b.y)) / d;

        double uy = ((a.x * a.x + a.y * a.y) * (c.x - b.x)
                     + (b.x * b.x + b.y * b.y) * (a.x - c.x)
                     + (c.x * c.x + c.y * c.y) * (b.x - a.x)) / d;

        Point center = new Point((int) ux, (int) uy);
        double radius = Math.sqrt((center.x - a.x) * (center.x - a.x) + (center.y - a.y) * (center.y - a.y));

        return new Circle(center, radius);
    }

 // Ova metoda sada ne samo da proverava preklapanje prilikom dodavanja ivice,
 // već kontinuirano uklanja loše ivice u svakom koraku.
 private void eliminateBadEdges() {
     Iterator<Edge> iterator = AEL.iterator();
     while (iterator.hasNext()) {
         Edge edge = iterator.next();
         // Provera preklapanja sa svim postojećim ivicama u AEL
         for (Edge existingEdge : AEL) {
             if (!edge.equals(existingEdge) && doEdgesIntersect(edge, existingEdge)) {
                 // Ako se ivice preklapaju, ukloni tu ivicu iz AEL
                 iterator.remove();
                 System.out.println("Removed bad edge due to overlap: " + edge);
                 break; // Prekinite iteraciju jer je ivica već uklonjena
             }
         }
     }
 }

 // Modifikovana verzija 'addEdgeToAEL' koja uključuje eliminaciju loših ivica pre nego što dodamo novu ivicu
 private void addEdgeToAEL(Edge e) {
     // Pre nego što dodamo novu ivicu, eliminisemo loše ivice
     eliminateBadEdges();
     
     // Ostatak funkcionalnosti za dodavanje ivice u AEL
     Edge flipped = new Edge(e.p2, e.p1);

     // Proveri da li flipovanje zadovoljava Delaunay uslove
     if (AEL.contains(flipped) && satisfiesDelaunayCondition(e, flipped)) {
         AEL.remove(flipped); // Ukloni flipped ivicu ako zadovoljava uslove
         System.out.println("Flipped edge: " + flipped);  // Log flipovane ivice
     } else if (!DT.contains(e)) {
         // Dodaj ivicu samo ako ne izaziva preklapanje
         if (!isPreliminaryCheckForOverlap(e)) {
             AEL.add(e);  // Dodaj ivicu
             System.out.println("Added edge to AEL: " + e);  // Log dodate ivice
         } else {
             System.out.println("Skipping edge due to potential overlap: " + e);
         }
     }
 }


    // Provera da li flipovanje zadovoljava Delaunay uslove
    private boolean satisfiesDelaunayCondition(Edge original, Edge flipped) {
        // Preuzmi četiri tačke koje čine dva trougla
        Point p1 = original.p1, p2 = original.p2, p3 = flipped.p1, p4 = flipped.p2;

        // Izračunaj circumkrug za oba trougla
        Circle c1 = circumCircle(p1, p2, p3);
        Circle c2 = circumCircle(p1, p3, p4);
        
        // Ako je circumkrug u skladu sa Delaunay uslovima, onda flipovanje ima smisla
        return (c1 != null && c2 != null && c1.radius < c2.radius);
    }

    // Proveri da li nova ivica uzrokuje preklapanje sa postojećim ivicama u AEL
    private boolean isPreliminaryCheckForOverlap(Edge newEdge) {
        for (Edge existingEdge : AEL) {
            // Proveri ako nova ivica preseca bilo koju postojeću
            if (doEdgesIntersect(existingEdge, newEdge)) {
                return true; // Preklapanje je detektovano
            }
        }
        return false; // Nema preklapanja
    }

    // Proverava da li dve ivice preseku
    private boolean doEdgesIntersect(Edge e1, Edge e2) {
        return ccw(e1.p1, e1.p2, e2.p1) != ccw(e1.p1, e1.p2, e2.p2) &&
               ccw(e2.p1, e2.p2, e1.p1) != ccw(e2.p1, e2.p2, e1.p2);
    }

    // Provera orijentacije tačaka (counter-clockwise)
    private boolean ccw(Point p1, Point p2, Point p3) {
        return (p3.y - p1.y) * (p2.x - p1.x) > (p2.y - p1.y) * (p3.x - p1.x);
    }


    private boolean isLeftOfEdge(Edge e, Point p) {
        return ((e.p2.x - e.p1.x) * (p.y - e.p1.y) - (p.x - e.p1.x) * (e.p2.y - e.p1.y)) > 0;
    }


    public void printTriangulation() {
        System.out.println("Delaunay Triangulation:");
        for (Edge edge : DT) {
            System.out.println(edge);
        }
    }

    public static class Edge {
        public Point p1, p2;

        public Edge(Point p1, Point p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Edge edge = (Edge) o;
            return (p1.equals(edge.p1) && p2.equals(edge.p2)) || (p1.equals(edge.p2) && p2.equals(edge.p1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                Math.min(p1.hashCode(), p2.hashCode()),
                Math.max(p1.hashCode(), p2.hashCode())
            );
        }

        @Override
        public String toString() {
            return String.format("Edge: (%d, %d) -> (%d, %d)", p1.x, p1.y, p2.x, p2.y);
        }
    }

    public static class Circle {
        public Point center;
        public double radius;

        public Circle(Point center, double radius) {
            this.center = center;
            this.radius = radius;
        }
    }
    
    public Set<Edge> getEdges() {
        return DT.stream()
                 .filter(edge -> !edge.p1.equals(edge.p2)) // Remove self-loops
                 .collect(Collectors.toSet()); // Return a cleaned-up set
    }
    
}




