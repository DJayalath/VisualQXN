package org.qxn.visual.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.H;

public class Circuit {

    public Canvas getCanvas() {
        return canvas;
    }

    private final Canvas canvas;
    private final GraphicsContext graphicsContext;

    public int getNumWires() {
        return numWires;
    }

    public void setNumWires(int numWires) {
        this.numWires = numWires;
    }

    private int numWires;

    public Circuit(int numWires) {
        this.canvas = new Canvas(colDist + boxWidth * maxGates + colDist * maxGates, rowDist + boxHeight * numWires + rowDist * numWires);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.numWires = numWires;
        this.components = new Component[numWires][maxGates];

        this.components[0][0] = new StandardGate(0, 0, 1, new H(0));
        this.components[0][1] = new CNOTGate(0, 1);
        this.components[1][2] = new SWAPGate(1, 2);


        select(0, 0);
    }

    public static final int maxGates = 10;
    public static final double boxWidth = 50;
    public static final double boxHeight = 50;

    public static final int rowDist = 20;
    public static final int colDist = 20;

    private int selectedRow;
    private int selectedCol;
    private double selectedHeight;

    private final Component[][] components;

    public static double getXFromCol(double col) {
        return colDist + col * (boxWidth + colDist);
    }

    public static double getYFromRow(double row) {
        return rowDist + row * (boxHeight + rowDist);
    }

    public void select(double x, double y) {

        // Find selected box
        x -= colDist;
        y -= rowDist;
        selectedCol = (int) (x / (boxWidth + colDist));
        selectedRow = (int) (y / (boxHeight + rowDist));

        // Expand selection to occupy component
        selectedHeight = boxHeight;
        for (int i = 0; i < numWires; i++)
            if (components[i][selectedCol] != null) {
                if (selectedRow >= i && selectedRow < i + components[i][selectedCol].getSpan()) {
                    selectedRow = i;
                    selectedHeight = boxHeight + (boxHeight + rowDist) * (components[i][selectedCol].getSpan() - 1);
                }
            }

        draw();
    }

    public void draw() {
        // Clear canvas
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw border
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw wires
        graphicsContext.setStroke(Color.BLACK);
        for (int i = 0; i < numWires; i++) {
            double y = getYFromRow(i) + boxHeight / 2.0;
            graphicsContext.strokeLine(0, y, colDist + boxWidth * (colDist + maxGates), y);
        }

        // Draw components
        for (int i = 0; i < numWires; i++)
            for (int j = 0; j < maxGates; j++)
                if (components[i][j] != null)
                    components[i][j].draw(graphicsContext);

        // Draw selection
        graphicsContext.setStroke(Color.RED);
        graphicsContext.strokeRect(colDist + selectedCol * (boxWidth + colDist), rowDist + selectedRow * (boxWidth + rowDist), boxWidth, selectedHeight);
    }

}
