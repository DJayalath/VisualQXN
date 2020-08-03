package org.qxn.visual.gui;

import javafx.collections.FXCollections;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import org.qxn.gates.*;
;import java.util.ArrayList;
import java.util.List;

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
    private final ChoiceBox<String> gateSelect;

    public Canvas getCanvas() {
        return canvas;
    }

    private final Canvas canvas;

    private final CircuitController circuitController;

    public ChoiceBox<String> getGateSelect() {
        return gateSelect;
    }

    public CircuitState(int numWires, int numGates, Canvas canvas, CircuitController circuitController) {

        this.circuitController = circuitController;

        this.numWires = numWires;
        this.numGates = numGates;

        this.canvas = canvas;
        canvas.setOnMouseEntered(e -> hoverEnabled = true);
        canvas.setOnMouseExited(e -> hoverEnabled = false);
        canvas.setOnMouseMoved(e -> hover(e.getX(), e.getY()));
        canvas.setOnMouseClicked(e -> select(e.getX(), e.getY()));
        resizeCanvas();

        components = new Component[numWires][numGates];

        addWireButton = new Button("Wire +");
        removeWireButton = new Button("Wire -");

        addWireButton.setOnMouseClicked(e -> addWire());
        removeWireButton.setOnMouseClicked(e -> removeWire());

        List<String> gates = new ArrayList<>();
        gates.add("H");
        gates.add("CNOT");
        gates.add("X");
        gates.add("Y");
        gates.add("Z");
        gateSelect = new ChoiceBox<>(FXCollections.observableArrayList(gates));
        gateSelect.setValue(gates.get(0));

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

        // Remove exposed components
        for (int j = 0; j < numGates; j++) {
            Integer r = getComponentRow(numWires - 1, j);
            if (r != null) {
                components[r][j] = null;
            }
        }

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

    private int hoverRow, hoverCol;
    private boolean hoverEnabled = false;

    private void hover(double x, double y) {
        hoverRow = getRowFromY(y);
        hoverCol = getColFromX(x);

        boolean last = hoverEnabled;
        hoverEnabled = isValidPosition(x, y, hoverRow, hoverCol);

        if (last != hoverEnabled)
            circuitController.notifyCanvasChange();
    }

    private int selectedRow, selectedCol, selectedSpan;
    private boolean selectedEnabled = false;

    private void select(double x, double y) {

        selectedEnabled = false;

        selectedRow = getRowFromY(y);
        selectedCol = getColFromX(x);

        if (isValidPosition(x, y, selectedRow, selectedCol)) {
            addGate(selectedRow, selectedCol);
            hover(x, y);
        } else {
            Integer r = getComponentRow(selectedRow, selectedCol);
            if (r != null) {
                selectedSpan = components[r][selectedCol].getSpan();
                selectedRow = r;
                selectedEnabled = true;
            }
        }

        circuitController.notifyCanvasChange();
    }

    private void addGate(int row, int col) {

        Component component = null;
        switch (gateSelect.getValue()) {
            case "H":
                component = new StandardGate("H", new H(row));
                break;
            case "X":
                component = new StandardGate("X", new X(row));
                break;
            case "Y":
                component = new StandardGate("Y", new Y(row));
                break;
            case "Z":
                component = new StandardGate("Z", new Z(row));
                break;
            case "CNOT":
                component = new CNOTGate(row);
                break;
            default: break;
        }

        if (component != null) {

            // Can it be added?
            if (row + component.getSpan() > numWires) {
                showErrorDialog("Cannot place the selected component in this position.");
                return;
            } else if (doesOverlapComponent(component, row, col)) {
                showErrorDialog("Cannot place the selected component in this position.");
                return;
            }

            components[row][col] = component;
        }

    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Circuit Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setWidth(150);
        alert.showAndWait();
    }

    private boolean isValidPosition(double x, double y, int row, int col) {
        boolean valid = !(x > getXFromCol(col) + gateWidth);
        if (valid)
            valid = !(x < getXFromCol(col));
        if (valid)
            valid = !(y > getYFromRow(row) + gateHeight);
        if (valid)
            valid = !(y < getYFromRow(row));
        if (valid) {
            valid = !containsComponent(row, col);
        }
        return valid;
    }

    private boolean doesOverlapComponent(Component component, int row, int col) {
        for (int i = 0; i < numWires; i++) {
            if (components[i][col] != null) {
                if (row >= i && row <= i + components[i][col].getSpan() - 1) {
                    return true;
                } else if (row + component.getSpan() - 1 >= i && row + component.getSpan() - 1 <= i + components[i][col].getSpan() - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsComponent(int row, int col) {
        for (int i = 0; i < numWires; i++) {
            if (components[i][col] != null) {
                if (row >= i && row <= i + components[i][col].getSpan() - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private Integer getComponentRow(int row, int col) {
        for (int i = 0; i < numWires; i++) {
            if (components[i][col] != null) {
                if (row >= i && row <= i + components[i][col].getSpan() - 1) {
                    return i;
                }
            }
        }
        return null;
    }

    private double getYFromRow(int row) {
        return (wireGap + gateHeight) * row + wireGap;
    }
    private double getXFromCol(int col) {
        return (gateGap + gateWidth) * col + gateGap;
    }
    private int getRowFromY(double y) {
        return (int) ((y - wireGap) / (wireGap + gateHeight));
    }
    private int getColFromX(double x) {
        return (int) ((x - gateGap) / (gateGap + gateWidth));
    }

    public void draw(GraphicsContext graphicsContext) {
        // Clear circuit
        graphicsContext.setFill(Color.WHITE);
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
                } else if (!containsComponent(i, j)) {
                    graphicsContext.setStroke(Color.rgb(0, 0, 0, 0.1));
                    graphicsContext.strokeRect(getXFromCol(j), getYFromRow(i), gateWidth, gateHeight);
                }
            }
        }

        // Draw hover
        if (hoverEnabled) {
            graphicsContext.setFill(Color.rgb(230, 230, 230, 0.8));
            graphicsContext.fillRect(getXFromCol(hoverCol), getYFromRow(hoverRow), gateWidth, gateHeight);
            graphicsContext.setStroke(Color.rgb(165, 137, 193, 1.0));
            graphicsContext.setLineWidth(2.5);
            graphicsContext.strokeLine(
                    getXFromCol(hoverCol) + gateWidth / 2.0 - 10, getYFromRow(hoverRow) + gateHeight / 2.0,
                    getXFromCol(hoverCol) + gateWidth / 2.0 + 10, getYFromRow(hoverRow) + gateHeight / 2.0
            );
            graphicsContext.strokeLine(
                    getXFromCol(hoverCol) + gateWidth / 2.0, getYFromRow(hoverRow) + gateHeight / 2.0 - 10,
                    getXFromCol(hoverCol) + gateWidth / 2.0, getYFromRow(hoverRow) + gateHeight / 2.0 + 10
            );
            graphicsContext.setLineWidth(1);
        }

        // Draw selected overlay on component
        if (selectedEnabled) {
            graphicsContext.setFill(Color.rgb(165, 137, 193, 0.4));
            graphicsContext.fillRect(
                    getXFromCol(selectedCol), getYFromRow(selectedRow), gateWidth,
                    gateHeight + (selectedSpan - 1) * (gateHeight + wireGap)
            );
        }
    }

}
