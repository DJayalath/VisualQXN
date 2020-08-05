package org.qxn.visual.gui;

import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.qxn.gates.*;
;import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CircuitState {

    public static final int maxWires = 10;
    public static final int maxGates = 20;

    public static final double wireGap = 20;
    public static final double gateGap = 20;

    public static final double gateWidth = 50;
    public static final double gateHeight = 50;
    private final Button addWireButton;
    private final Button removeWireButton;
    private final Button removeComponentButton;

    public Button getControlButton() {
        return controlButton;
    }

    private final Button controlButton;

    private final ChoiceBox<String> gateSelect;
    private final Canvas canvas;
    private final CircuitController circuitController;

    public Component[][] getComponents() {
        return components;
    }

    private Component[][] components;

    public int getNumWires() {
        return numWires;
    }

    public int getNumGates() {
        return numGates;
    }

    private int numWires;
    private int numGates;
    private int hoverRow, hoverCol;
    private boolean hoverEnabled = false;
    private int selectedRow, selectedCol, selectedSpan;
    private boolean selectedEnabled = false;

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
        removeComponentButton = new Button("DELETE");
        removeComponentButton.setDisable(true);
        controlButton = new Button("Control");
        controlButton.setDisable(true);

        addWireButton.setOnMouseClicked(e -> addWire());
        removeWireButton.setOnMouseClicked(e -> removeWire());
        removeComponentButton.setOnMouseClicked(e -> removeComponent(selectedRow, selectedCol));
        controlButton.setOnMouseClicked(e -> control(selectedRow, selectedCol));

        List<String> gates = new ArrayList<>();
        gates.add("H");
        gates.add("X");
        gates.add("Y");
        gates.add("Z");
        gates.add("CNOT");
        gates.add("Measure");
        gateSelect = new ChoiceBox<>(FXCollections.observableArrayList(gates));
        gateSelect.setValue(gates.get(0));

        updateButtons();
    }

    public Button getAddWireButton() {
        return addWireButton;
    }

    public Button getRemoveWireButton() {
        return removeWireButton;
    }

    public Button getRemoveComponentButton() {
        return removeComponentButton;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public ChoiceBox<String> getGateSelect() {
        return gateSelect;
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
        circuitController.notifyCircuitStateChange();
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
        circuitController.notifyCircuitStateChange();
        updateButtons();
    }

    private void resizeCanvas() {
        canvas.setWidth( (gateGap + gateWidth) * numGates + gateGap );
        canvas.setHeight( (wireGap + gateHeight) * numWires + wireGap );
    }

    private void hover(double x, double y) {
        hoverRow = getRowFromY(y);
        hoverCol = getColFromX(x);

        boolean last = hoverEnabled;
        hoverEnabled = isValidPosition(x, y, hoverRow, hoverCol);

        if (last != hoverEnabled)
            circuitController.notifyCircuitChange();

    }

    private void select(double x, double y) {

        int lastRow = selectedRow;
        int lastCol = selectedCol;
        boolean lastEnabled = selectedEnabled;

        selectedEnabled = false;

        selectedRow = getRowFromY(y);
        selectedCol = getColFromX(x);

        if (isValidPosition(x, y, selectedRow, selectedCol)) {
            addComponent(selectedRow, selectedCol);
            hover(x, y);
        } else {
            Integer r = getComponentRow(selectedRow, selectedCol);
            if (r != null) {
                selectedSpan = components[r][selectedCol].getSpan();
                selectedRow = r;
                selectedEnabled = true;
            }
        }

        removeComponentButton.setDisable(!selectedEnabled);
        controlButton.setDisable(!(canControl(selectedRow, selectedCol) && selectedEnabled));

        // Classical control
        if (selectedEnabled && lastEnabled && lastRow == selectedRow && lastCol == selectedCol &&
                !components[selectedRow][selectedCol].isGate()) {
            circuitController.notifyIndicatorLabelChange("Select component to connect to measurement device");
            circuitController.notifyIndicatorBarChange(Color.ORANGE);
        } else if (selectedEnabled && components[selectedRow][selectedCol].isGate()) {
            if (components[lastRow][lastCol] != null && !components[lastRow][lastCol].isGate()) {
                StandardGate gate = (StandardGate) components[selectedRow][selectedCol];
                QuantumMeter meter = (QuantumMeter) components[lastRow][lastCol];
                gate.setQuantumMeter(meter);
                circuitController.notifyCircuitStateChange();
                circuitController.notifyIndicatorLabelChange("We good lads");
                circuitController.notifyIndicatorBarChange(Color.rgb(0, 200, 0, 0.5));
            }
        } else {
            circuitController.notifyIndicatorLabelChange("We good lads");
            circuitController.notifyIndicatorBarChange(Color.rgb(0, 200, 0, 0.5));
        }

        circuitController.notifyCircuitChange();
    }

    private void control(int row, int col) {
        components[row - 1][col] = new ControlledGate((StandardGate) components[row][col]);
        components[row][col] = null;
        selectedRow--;
        selectedSpan++;
        controlButton.setDisable(!(canControl(selectedRow, selectedCol) && selectedEnabled));
        circuitController.notifyCircuitStateChange();
    }

    private boolean canControl(int row, int col) {
        if (row <= 0) return false;
        if (containsComponent(row - 1, col)) return false;
        return true;
    }

    private void addComponent(int row, int col) {

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
            case "Measure":
                component = new QuantumMeter(row, col);
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
            circuitController.notifyCircuitStateChange();
        }

    }

    private void removeComponent(int row, int col) {
        if (components[row][col] != null && selectedEnabled) {
            components[row][col].cleanUp();
            components[row][col] = null;
            selectedEnabled = false;
            removeComponentButton.setDisable(true);
            controlButton.setDisable(true);
            circuitController.notifyCircuitStateChange();
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

        if (col == numGates - 1)
            return false;

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

    public static double getYFromRow(int row) {
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
            if (!components[selectedRow][selectedCol].isGate()) {

                graphicsContext.setFill(Color.rgb(230, 230, 230, 0.95));
                graphicsContext.fillRect(
                        getXFromCol(selectedCol), getYFromRow(selectedRow), gateWidth,
                        gateHeight + (selectedSpan - 1) * (gateHeight + wireGap)
                );

                graphicsContext.setStroke(Color.rgb(0, 200, 0, 0.5));
                graphicsContext.setLineWidth(3);
                graphicsContext.strokeLine(
                        getXFromCol(selectedCol) + gateWidth / 2.0 - 10, getYFromRow(selectedRow) + gateHeight / 2.0,
                        getXFromCol(selectedCol) + gateWidth / 2.0 + 10, getYFromRow(selectedRow) + gateHeight / 2.0
                );
                graphicsContext.strokeLine(
                        getXFromCol(selectedCol) + gateWidth / 2.0, getYFromRow(selectedRow) + gateHeight / 2.0 - 10,
                        getXFromCol(selectedCol) + gateWidth / 2.0, getYFromRow(selectedRow) + gateHeight / 2.0 + 10
                );
                graphicsContext.setLineWidth(1);
            } else {
                graphicsContext.setFill(Color.rgb(165, 137, 193, 0.4));
                graphicsContext.fillRect(
                        getXFromCol(selectedCol), getYFromRow(selectedRow), gateWidth,
                        gateHeight + (selectedSpan - 1) * (gateHeight + wireGap)
                );
            }
        }

        drawMeasurements(graphicsContext);
    }

    private double[] measurements;

    public void setMeasurements(double[] measurements) {
        this.measurements = measurements;
    }

    private void drawMeasurements(GraphicsContext graphicsContext) {

        if (measurements == null) {
            measurements = new double[numWires];
            Arrays.fill(measurements, 0);
        } else if (measurements.length != numWires) {
            double[] newMeasurements = new double[numWires];
            for (int i = 0; i < Math.min(measurements.length, numWires); i++)
                newMeasurements[i] = measurements[i];
            measurements = newMeasurements;
        }

        for (int i = 0; i < numWires; i++) {

            graphicsContext.setFill(Color.WHITESMOKE);
            graphicsContext.fillRect(getXFromCol(numGates - 1), getYFromRow(i), gateWidth, gateHeight);

            graphicsContext.setFill(Color.rgb(0, 200, 0, 0.5));
            graphicsContext.fillRect(getXFromCol(numGates  - 1), getYFromRow(i) + gateHeight - gateHeight * measurements[i], gateWidth, gateHeight * measurements[i]);

            String percentage = String.format("%.1f", measurements[i] * 100.0);
            graphicsContext.setTextAlign(TextAlignment.CENTER);
            graphicsContext.setTextBaseline(VPos.CENTER);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.fillText(percentage + "%", getXFromCol(numGates - 1) + gateWidth / 2.0, getYFromRow(i) + gateHeight / 2.0, gateWidth - 10);

        }

    }

}
