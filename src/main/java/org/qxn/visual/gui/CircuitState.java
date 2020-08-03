package org.qxn.visual.gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;;

public class CircuitState {

    public static final int maxWires = 10;
    public static final int maxGates = 20;

    public static final double wireGap = 20;
    public static final double gateGap = 20;

    public static final double gateWidth = 50;
    public static final double gateHeight = 50;

    private Component[][] components;

    private int numWires;
    private int numGates;

    public Button getAddWireButton() {
        return addWireButton;
    }

    public Button getRemoveWireButton() {
        return removeWireButton;
    }

    private final Button addWireButton;
    private final Button removeWireButton;

    public Canvas getCanvas() {
        return canvas;
    }

    private final Canvas canvas;

    private final CircuitController circuitController;

    public CircuitState(int numWires, int numGates, Canvas canvas, CircuitController circuitController) {

        this.circuitController = circuitController;

        this.numWires = numWires;
        this.numGates = numGates;

        this.canvas = canvas;
        resizeCanvas();

        components = new Component[numWires][numGates];

        addWireButton = new Button("Wire +");
        removeWireButton = new Button("Wire -");

        addWireButton.setOnMouseClicked(e -> addWire());
        removeWireButton.setOnMouseClicked(e -> removeWire());

        updateButtons();
    }

    private void updateButtons() {
        addWireButton.setDisable(numWires >= maxWires);
        removeWireButton.setDisable(numWires <= 1);
    }

    public void setNumGates(int numGates) {

        Component[][] temp = new Component[numWires][numGates];

        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < Math.min(this.numGates, numGates); j++) {
                temp[i][j] = components[i][j];
            }
        }

        components = temp;

        this.numGates = numGates;
        resizeCanvas();
        circuitController.notifyCanvasChange();
    }

    private void addWire() {
        setNumWires(numWires + 1);
    }

    private void removeWire() {
        setNumWires(numWires - 1);
    }

    private void setNumWires(int numWires) {

        Component[][] temp = new Component[numWires][numGates];

        for (int i = 0; i < Math.min(this.numWires, numWires); i++) {
            for (int j = 0; j < numGates; j++) {
                temp[i][j] = components[i][j];
            }
        }

        components = temp;

        this.numWires = numWires;
        resizeCanvas();
        circuitController.notifyCanvasChange();
        updateButtons();
    }

    private void resizeCanvas() {
        canvas.setWidth( (gateGap + gateWidth) * numGates + gateGap );
        canvas.setHeight( (wireGap + gateHeight) * numWires + wireGap );
    }

    private double getYFromRow(double row) {
        return (wireGap + gateHeight) * row + wireGap;
    }

    private double getXFromCol(double col) {
        return (gateGap + gateWidth) * col + gateGap;
    }

    public void draw(GraphicsContext graphicsContext) {
        // Clear circuit
        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw wires
        graphicsContext.setStroke(Color.BLACK);
        for (int i = 0; i < numWires; i++) {
            double y = getYFromRow(i) + gateHeight / 2.0;
            graphicsContext.strokeLine(0, y, canvas.getWidth(), y);
        }

        // Draw components
        for (int j = 0; j < numGates; j++) {
            for (int i = 0; i < numWires; i++) {
                if (components[i][j] != null) {
                    double x = getXFromCol(j);
                    double y = getYFromRow(i);
                    components[i][j].draw(x, y, graphicsContext);
                }
            }
        }
    }

}
