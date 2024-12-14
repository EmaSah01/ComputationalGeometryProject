package triangulation;

import java.awt.Point;

public class Edge {
    public Point start;
	public Point end;

    public Edge(Point start, Point end) {
        this.start = start;
        this.end = end;
    }
    
    public Point getStart() {
        return start;
    }

    public Point getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Edge: (" + start.x + ", " + start.y + ") -> (" + end.x + ", " + end.y + ")";
    }
}