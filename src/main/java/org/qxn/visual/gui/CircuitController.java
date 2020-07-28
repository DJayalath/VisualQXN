package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import org.qxn.QuantumMachine;
import org.qxn.gates.*;

import java.util.ArrayList;
import java.util.List;

public class CircuitController {

    public static final double LEFT_PADDING = 26;
    public static final double TOP_PADDING = 50;
    public static final double LINE_GAP = 60;
    public static final double GRID_GAP = 60;
    private static final int MAX_GATES = 10;
    final BarChart<String, Number> barChart;
    private final GraphicsContext graphicsContext;
    List<double[]> probabilities = new ArrayList<>();
    List<Integer> breakPoints = new ArrayList<>();
    private int numWires;
    private int selectedX, selectedY;
    private double selectedHeight;
    private VGate[][] vGates;
    private XYChart.Series<String, Number> series;
    private QuantumMachine quantumMachine;
    private int step = 0;
    private VGate selected;
    private boolean connecting = false;

    public CircuitController(int numWires, GraphicsContext graphicsContext) {
        quantumMachine = new QuantumMachine(numWires);
        vGates = new VGate[10][MAX_GATES];
        this.numWires = numWires;
        this.graphicsContext = graphicsContext;
        selectedX = 0;
        selectedY = 0;

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("Output");
        yAxis.setLabel("Probability");

        barChart.setLegendVisible(false);
        barChart.setPrefWidth(600);

        series = new XYChart.Series<>();
        for (int i = 0; i < 1 << numWires; i++) {
            String binary = String.format("%" + numWires + "s",
                    Integer.toBinaryString(i)).replace(' ', '0');
            series.getData().add(new XYChart.Data<>(binary, (i == 0) ? 1.0 : 0.0));
        }
        barChart.getData().add(series);
    }

    public int getNumWires() {
        return numWires;
    }

    public int getSelectedX() {
        return selectedX;
    }

    public int getSelectedY() {
        return selectedY;
    }

    public BarChart<String, Number> getBarChart() {
        return barChart;
    }

    private void resetExecution(boolean wiresChanged) {

        if (wiresChanged) {
            series = new XYChart.Series<>();
            for (int i = 0; i < 1 << numWires; i++) {
                String binary = String.format("%" + numWires + "s",
                        Integer.toBinaryString(i)).replace(' ', '0');
                series.getData().add(new XYChart.Data<>(binary, (i == 0) ? 1.0 : 0.0));
            }
            barChart.getData().set(0, series);
            quantumMachine = new QuantumMachine(numWires);
        }

        step = 0;
        probabilities.clear();
        breakPoints.clear();
        draw();
    }

    public void select(double x, double y) {

        x = x - LEFT_PADDING / 2;
        y = y - TOP_PADDING / 2;
        selectedX = (int) (x / LINE_GAP);
        selectedY = (int) (y / GRID_GAP);

        if (connecting) {
            if (selected != null) {
                VGate toConnect = find(selectedX, selectedY);
                if (toConnect != null) {
                    selected.setConnected(toConnect);
                }
            }
            connecting = false;
            selected = null;
        }

        selectedHeight = 50.0;
        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < MAX_GATES; j++) {
                if (vGates[i][j] != null) {
                    if (selectedX == vGates[i][j].getGridX() &&
                            selectedY >= vGates[i][j].getGridY() &&
                            selectedY <= vGates[i][j].getGridY() + (vGates[i][j].getSpan() - 1)) {
                        selectedY = vGates[i][j].getGridY();
                        selectedHeight = 50 + (vGates[i][j].getSpan() - 1) * LINE_GAP;
                    }
                }
            }
        }

        draw();
    }

    private boolean isGatePresent(int x, int y) {
        return vGates[x][y] != null;
    }

    public void addGate(VGate gate) throws CircuitPlacementException {

        selectedHeight = 50 + (gate.getSpan() - 1) * LINE_GAP;

        selectedX = gate.getGridX();
        selectedY = gate.getGridY();

        // Check if gate not blocking space
        for (int i = 0; i < gate.getSpan(); i++) {
            if (isGatePresent(selectedX, selectedY + i)) {
                throw new CircuitPlacementException("Cannot place this gate here");
            }
        }

        vGates[selectedY][selectedX] = gate;
        System.out.println("ADDED " + selectedX + " " + selectedY);
        resetExecution(false);
        draw();
    }

    public void removeGate() {

        vGates[selectedY][selectedX] = null;
        selectedHeight = 50;
        resetExecution(false);
        draw();

    }

    public void addWire() {
        numWires++;
        resetExecution(true);
        draw();
    }

    public void removeWire() {

        numWires--;

        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < MAX_GATES; j++) {

                if (vGates[i][j] != null) {
                    if (vGates[i][j].getGridY() + (vGates[i][j].getSpan() - 1) >= numWires)
                        vGates[i][j] = null;
                }
            }
        }

        if (selectedY >= numWires) {
            selectedY = numWires - 1;
        }

        selectedHeight = 50.0;
        resetExecution(true);
        draw();
    }

    private void next() {

        for (int j = 0; j < MAX_GATES; j++) {
            for (int i = 0; i < numWires; i++) {

                VGate g = vGates[i][j];

                if (g != null) {

                    boolean canAdd = true;
                    if (g.getConnected() != null) {
                        VGate connected = g.getConnected();
                        canAdd = connected.getValue() == 1;
                    }

                    // REMEMBER TO BREAK
                    if (canAdd) {
                        switch (g.getLabel()) {
                            case "BP":
                                double[] results = new double[1 << numWires];
                                for (int k = 0; k < 1 << numWires; k++) {
                                    results[k] = quantumMachine.getQubits().data[k][0].getMagnitude2();
                                }
                                probabilities.add(results);
                                breakPoints.add(j);
                                break;
                            case "M":
                                int m = quantumMachine.measure(g.getGridY());
                                g.setValue(m);
                                break;
                            case "X":
                                quantumMachine.addGate(new X(g.getGridY()));
                                break;
                            case "Y":
                                quantumMachine.addGate(new Y(g.getGridY()));
                                break;
                            case "Z":
                                quantumMachine.addGate(new Z(g.getGridY()));
                                break;
                            case "H":
                                quantumMachine.addGate(new H(g.getGridY()));
                                break;
                            case "SWAP":
                                quantumMachine.addGate(new SWAP(g.getGridY(), g.getGridY() + 1));
                            case "CNOT":
                                quantumMachine.addGate(new CNOT(g.getGridY(), g.getGridY() + 1));
                                break;
                            default:
                                break;
                        }
                        quantumMachine.execute();
                    }
                }
            }
        }

        breakPoints.add(MAX_GATES - 1);
        double[] results = new double[1 << numWires];
        for (int k = 0; k < 1 << numWires; k++) {
            results[k] = quantumMachine.getQubits().data[k][0].getMagnitude2();
        }
        probabilities.add(results);

    }

    private void updateBarChart() {
//        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int i = 0; i < 1 << numWires; i++) {
            System.out.println(probabilities.get(step)[i]);
            String binary = String.format("%" + numWires + "s",
                    Integer.toBinaryString(i)).replace(' ', '0');
            series.getData().set(i, new XYChart.Data<>(binary, probabilities.get(step)[i]));
        }
        barChart.getData().set(0, series);
    }

    public void execute() {
        resetExecution(true);
        next();
        step = 0;
        updateBarChart();
        draw();
    }

    public void stepForward() {
        step++;
        updateBarChart();
        draw();
    }

    public void stepBackward() {
        step--;
        updateBarChart();
        draw();
    }

    private VGate find(int x, int y) {
        return vGates[y][x];
    }

    public void connect() {
        selected = find(selectedX, selectedY);
        connecting = true;
    }

    public void draw() {

        graphicsContext.clearRect(0, 0, graphicsContext.getCanvas().getWidth(), graphicsContext.getCanvas().getHeight());

        // Draw lines
        graphicsContext.setStroke(Color.BLACK);
        for (int i = 0; i < numWires; i++) {
            graphicsContext.strokeLine(0, TOP_PADDING + i * LINE_GAP, 600, TOP_PADDING + i * LINE_GAP);
        }

        // Draw gates
        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < MAX_GATES; j++) {
                if (vGates[i][j] != null) {
                    vGates[i][j].draw(graphicsContext);
                    System.out.println("DREW " + i + " " + j);
                }
            }
        }

        // Draw selection
        graphicsContext.setStroke(Color.RED);
        graphicsContext.strokeRect((selectedX * LINE_GAP + LEFT_PADDING) - 25, (selectedY * GRID_GAP + TOP_PADDING) - 25, 50, selectedHeight);

        // Draw step
        if (!breakPoints.isEmpty()) {
            graphicsContext.setFill(Color.LIGHTGREEN);
            graphicsContext.fillOval(LEFT_PADDING + breakPoints.get(step) * GRID_GAP - 5, 0, 10, 10);
        }
    }


}
