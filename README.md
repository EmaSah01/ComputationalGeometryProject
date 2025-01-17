# Computational Geometry Project

## Overview
This project is a graphical framework for visualizing and interacting with various computational geometry algorithms. It includes implementations for Delaunay Triangulation, Voronoi Diagram, Convex Hull (using Gift Wrapping and Graham Scan algorithms) and k-d Tree construction.

## Features
- **Delaunay Triangulation**: Generate and visualize Delaunay edges for a set of points.
- **Voronoi Diagram**: Construct and display Voronoi edges and vertices.
- **Convex Hull**: Compute the convex hull using Gift Wrapping and Graham Scan algorithms.
- **k-d Tree**: Build and visualize k-d Tree structures for given points.
- **Interactive GUI**: Add, remove, and drag points directly on the canvas.
- **Random Point Generation**: Populate the canvas with random points for testing.

### Usage
- Add points by left-clicking on the canvas.
- Remove points by right-clicking on them.
- Use the menu options to compute and visualize algorithms:
  - Convex Hull (Gift Wrapping or Graham Scan)
  - Triangulation
  - Delaunay Triangulation
  - Voronoi Diagram
  - k-d Tree
- Use the "Random dots" button to generate random points.
- Clear the canvas using the "Clear Scene" button.

## Project Structure
- **frame**: Contains the main GUI framework and event handling.
- **triangulation**: Implements triangulation-related algorithms.
- **convex_hull**: Includes Gift Wrapping and Graham Scan algorithms.
- **kdtree**: Implements k-d Tree construction and visualization.
- **voronoi**: Contains the logic for Voronoi diagram construction.


