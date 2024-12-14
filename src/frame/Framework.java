package frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;


import triangulation.Triangulation;
import triangulation.Triangulation.Edge;
import voronoi.VoronoiDiagram;
import voronoi.VoronoiDiagram.LineSegment;
import triangulation.DelaunayTriangulation;
import kdtree.KdTree;
import convex_hull.GiftWrapping;
import convex_hull.GrahamScan;

public class Framework extends JPanel {
    private ArrayList<Point> points = new ArrayList<>();
    private ArrayList<Point> hullPoints = new ArrayList<>();
    private Point selectedPoint;
    private ArrayList<triangulation.Triangulation.Edge> triangulationEdges = new ArrayList<>();
    private KdTree kdtree;
    private ArrayList<LineSegment> voronoiEdges = new ArrayList<>();

    
    private boolean showKDTree = false;
    private boolean showConvexHull = false;
    private boolean showTriangulation = false;

    
    private static final int RANDOM_POINT_COUNT = 5;
    
    public Framework() {
        setFocusable(true);
        setBackground(Color.WHITE);
        initializeMouseListeners();
    }
    
    

    private void initializeMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedPoint = null; // Reset selected point
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedPoint != null) {
                    selectedPoint.setLocation(e.getPoint());
                    repaint();
                }
            }
        });
    }

    private void handleMousePressed(MouseEvent e) {
        Point clickedPoint = e.getPoint();
        selectedPoint = findNearestPoint(clickedPoint, 10);
        
        if (SwingUtilities.isRightMouseButton(e)) {
            if (selectedPoint != null) {
                removePoint(selectedPoint);
            }
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            if (selectedPoint == null) {
                addPoint(e.getPoint());
            }
        }
    }

    public void addPoint(Point point) {
        points.add(point);
        repaint();
    }
    
    public void removePoint(Point point) {
        points.remove(point);
        repaint();
    }
    

    public void clearScene() {
        points.clear();
        hullPoints.clear();
        triangulationEdges.clear();
        kdtree = null;
        showKDTree = false;
        showConvexHull = false;
        showTriangulation = false;
        repaint();
    }
    
    public void generateKDTree() {
        if (points.size() > 1) {
            kdtree = new KdTree(points);
            repaint();
        }
    }

    public void calculateConvexHull() {
    	triangulationEdges.clear();
        hullPoints = GiftWrapping.findConvexHull(points);
        showConvexHull = true;
        repaint();
    }

    public void calculateConvexHullGrahamScan() {
    	triangulationEdges.clear();
        hullPoints = GrahamScan.findConvexHull(points);
        showConvexHull = true;
        repaint();
    }

    private Point findNearestPoint(Point p, int threshold) {
        return points.stream().filter(point -> point.distance(p) < threshold).findFirst().orElse(null);
    }

    public void calculateTriangulation() {
        calculateConvexHull(); // Ensure the convex hull is calculated
        triangulationEdges.clear(); // Clear previous triangulation
        Triangulation triangulation = new Triangulation(points);
        triangulationEdges = new ArrayList<>(triangulation.triangulate());
        showConvexHull = true;  // Ensure convex hull is displayed
        showTriangulation = true; // Ensure triangulation is displayed
        repaint();
    }

    public void performDelaunayTriangulation() {
        System.out.println("Processing edges...");

        new Thread(() -> {
            try {
                // Create a DelaunayTriangulation instance with the current points
                DelaunayTriangulation delaunay = new DelaunayTriangulation(points);

                // Perform triangulation
                delaunay.performTriangulation();

                // Retrieve the edges and map them to Triangulation.Edge
                List<Triangulation.Edge> edges = new ArrayList<>();
                for (DelaunayTriangulation.Edge edge : delaunay.getEdges()) {
                    edges.add(new Triangulation.Edge(edge.p1, edge.p2));
                }

                // Safely update GUI on the Event Dispatch Thread
                SwingUtilities.invokeLater(() -> {
                    triangulationEdges.clear(); // Clear old edges if necessary
                    triangulationEdges.addAll(edges); // Add the new triangulation edges
                    repaint(); // Repaint the GUI to display updated edges
                    System.out.println("Delaunay triangulation complete.");
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    System.err.println("Error during triangulation: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }



    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawPoints(g); 
        if (showConvexHull) {
            drawConvexHull(g);
        }

        if (showTriangulation) {
            drawTriangulationEdges(g);
        }

        if (showKDTree && kdtree != null && kdtree.root != null) {
            drawKDTree(g, kdtree.root);
        }

        // Add drawing of Voronoi edges
        if (voronoiEdges != null && !voronoiEdges.isEmpty()) {
            drawVoronoi(g);
        }
    }


    private void drawTriangulationEdges(Graphics g) {
        if (!triangulationEdges.isEmpty()) {
            g.setColor(Color.RED);  // Postavljamo boju za ivice
            for (Triangulation.Edge edge : triangulationEdges) {
                if (edge.start != null && edge.end != null) {
                    // Ispisujemo koordinate ivica (u logu) za debug
                    System.out.println("Drawing edge: " + edge.start + " to " + edge.end);
                    g.drawLine(edge.start.x, edge.start.y, edge.end.x, edge.end.y);  // Crtamo ivicu
                }
            }
        }
    }


    private void drawPoints(Graphics g) {
        g.setColor(Color.BLACK);
        for (Point point : points) {
            g.fillOval(point.x - 4, point.y - 4, 8, 8);
        }
    }

    private void drawConvexHull(Graphics g) {
        g.setColor(Color.RED);
        for (int i = 0; i < hullPoints.size(); i++) {
            Point p1 = hullPoints.get(i);
            Point p2 = hullPoints.get((i + 1) % hullPoints.size());
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }
    
    private void drawKDTree(Graphics g, KdTree.Node node) {
        if (node == null) return;
        
        g.setColor(Color.RED);
        g.fillOval(node.point.x - 4, node.point.y - 4, 8, 8); // Draw the point

        if (node.left != null) {
            g.drawLine(node.point.x, node.point.y, node.left.point.x, node.left.point.y);
            drawKDTree(g, node.left); // Recur for left subtree
        }

        if (node.right != null) {
            g.drawLine(node.point.x, node.point.y, node.right.point.x, node.right.point.y);
            drawKDTree(g, node.right); // Recur for right subtree
        }
    }


    private void generateRandomPoints() {
        int width = getWidth();
        int height = getHeight();
        java.util.Random random = new java.util.Random();

        for (int i = 0; i < RANDOM_POINT_COUNT; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            points.add(new Point(x, y));
        }
        repaint(); 
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Computational Geometry Project");
            Framework visualizer = new Framework();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 800);
            frame.add(visualizer);
            frame.setVisible(true);
            createMenu(frame, visualizer);
        });
    }

    private static void createMenu(JFrame frame, Framework visualizer) {
        JMenuBar menubar = new JMenuBar();
        JMenu convexHullMenu = new JMenu("Convex Hull");
        menubar.add(convexHullMenu);
        
        addMenuItem(convexHullMenu, "Gift Wrapping", e -> {
            visualizer.calculateConvexHull();
            visualizer.showKDTree = false; 
            visualizer.showConvexHull = true; 
            visualizer.showTriangulation = false; 
        });
        addMenuItem(convexHullMenu, "Graham Scan", e -> {
            visualizer.calculateConvexHullGrahamScan();
            visualizer.showKDTree = false; 
            visualizer.showConvexHull = true; 
            visualizer.showTriangulation = false; 
        });
        
        addMenuItem(menubar, "Triangulation", e -> {
            visualizer.calculateTriangulation();
            visualizer.showKDTree = false; // Hide k-d tree
            visualizer.showConvexHull = true; // Hide convex hull
            visualizer.showTriangulation = true; // Show triangulation
        });
        
        addMenuItem(menubar, "Generate k-D Tree", e -> {
            visualizer.generateKDTree();
            visualizer.showKDTree = true; 
            visualizer.showConvexHull = false; 
            visualizer.showTriangulation = false; 
        });
        
        addMenuItem(menubar, "Delaunay Triangulation", e -> {
            visualizer.performDelaunayTriangulation();
            visualizer.showKDTree = false;
            visualizer.showConvexHull = false;
            visualizer.showTriangulation = true; // Make sure triangulation is displayed
        });

        // Add the new Voronoi Diagram menu item
        addMenuItem(menubar, "Voronoi Diagram", e -> {
            visualizer.calculateVoronoi();
            visualizer.showKDTree = false;
            visualizer.showConvexHull = false;
            visualizer.showTriangulation = false; // Hide triangulation
        });

        JButton randomPointsButton = new JButton("Random dots");
        randomPointsButton.addActionListener(e -> visualizer.generateRandomPoints());
        JButton clearButton = new JButton("Clear Scene");
        clearButton.addActionListener(e -> visualizer.clearScene());

        JPanel controlPanel = new JPanel();
        controlPanel.add(randomPointsButton);
        controlPanel.add(clearButton);

        frame.setJMenuBar(menubar);
        frame.add(controlPanel, BorderLayout.SOUTH);
    }




    public void calculateVoronoi() {
        // Ensure Delaunay triangulation is already computed
        if (triangulationEdges.isEmpty()) {
            performDelaunayTriangulation(); // Ensure Delaunay triangulation is performed
        }

        // Create a DelaunayTriangulation object using points and triangulationEdges
        DelaunayTriangulation delaunay = new DelaunayTriangulation(points, triangulationEdges);

        // Create a Voronoi diagram using the Delaunay triangulation
        VoronoiDiagram voronoi = new VoronoiDiagram(delaunay); // Pass the Delaunay triangulation to the VoronoiDiagram

        // Now update the visualizer to show the Voronoi diagram
        repaint();
    }



     private void drawVoronoi(Graphics g) {
    	    if (voronoiEdges != null && !voronoiEdges.isEmpty()) {
    	        g.setColor(Color.GREEN);  // Use green for Voronoi edges
    	        for (LineSegment segment : voronoiEdges) {
    	            g.drawLine(segment.p1.x, segment.p1.y, segment.p2.x, segment.p2.y);
    	        }
    	    }
    	}




	private static void addMenuItem(JMenu menu, String title, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(listener);
        menu.add(menuItem);
    }

    private static void addMenuItem(JMenuBar menubar, String title, ActionListener listener) {
        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(listener);
        menubar.add(menuItem);
    }
}
