package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
import org.qxn.QuantumMachine;
import org.qxn.gates.*;

import java.util.Comparator;

public class CircuitController {

    public static final double LEFT_PADDING = 26;
    public static final double TOP_PADDING = 30;
    public static final double LINE_GAP = 60;
    public static final double GRID_GAP = 60;

    private int numWires;

    public int getSelectedX() {
        return selectedX;
    }

    public int getSelectedY() {
        return selectedY;
    }

    private int selectedX, selectedY;
    private double selectedHeight;

    private final GraphicsContext graphicsContext;

    private VGate[][] vGates;

    private static final int MAX_GATES = 10;

    public CircuitController(int numWires, GraphicsContext graphicsContext) {
        vGates = new VGate[10][MAX_GATES];
        this.numWires = numWires;
        this.graphicsContext = graphicsContext;
        selectedX = 0;
        selectedY = 0;
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
            if (isGatePresent(selectedX , selectedY + i)) {
                throw new CircuitPlacementException("Cannot place this gate here");
            }
        }

        vGates[selectedY][selectedX] = gate;
        System.out.println("ADDED " + selectedX + " " + selectedY);
        draw();
    }

    public void removeGate() {

        vGates[selectedY][selectedX] = null;
        selectedHeight = 50;

        draw();

    }

    private double[] probabilities;

    public void addWire() {
        numWires++;
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
//        for (VGate g : vGates) {
//            if (selectedX >= g.getGridX() && selectedX <= g.getGridX() + (g.getSpan() - 1) && g.getGridY() == selectedY) {
//                selectedX = g.getGridX();
//                selectedHeight = 50 + (g.getSpan() - 1) * LINE_GAP;
//            }
//        }

        draw();
    }

    public void run(BarChart<String, Number> barChart) {

        QuantumMachine quantumMachine = new QuantumMachine(numWires);

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

        probabilities = new double[1 << numWires];
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 1 << numWires; i++) {
            probabilities[i] = quantumMachine.getQubits().data[i][0].getMagnitude2();
            String binary = String.format("%" + numWires + "s",
                    Integer.toBinaryString(i)).replace(' ', '0');
            series.getData().add(new XYChart.Data<>(binary, probabilities[i]));
        }

        barChart.getData().set(0, series);

    }

    private VGate find(int x, int y) {

        return vGates[y][x];
    }

    private VGate selected;
    private boolean connecting = false;
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
    }


}
