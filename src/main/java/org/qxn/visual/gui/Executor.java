package org.qxn.visual.gui;

import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import org.qxn.QuantumMachine;
import org.qxn.linalg.ComplexMatrix;

import java.util.ArrayList;
import java.util.List;

public class Executor {

    private final List<double[]> probabilities;
    private final List<double[]> measurements;
    private final List<Integer> breakpoints;

    public ProbabilityChart getProbabilityChart() {
        return probabilityChart;
    }

    private final ProbabilityChart probabilityChart;
    private int breakpointIndex = 0;

    public Button getStepForwardButton() {
        return stepForwardButton;
    }

    public Button getStepBackwardButton() {
        return stepBackwardButton;
    }

    private final Button stepForwardButton;
    private final Button stepBackwardButton;

    private final CircuitController circuitController;

    public Executor(CircuitController circuitController) {
        this.circuitController = circuitController;

        probabilities = new ArrayList<>();
        measurements = new ArrayList<>();
        breakpoints = new ArrayList<>();
        stepBackwardButton = new Button("Step Backward");
        stepForwardButton = new Button("Step Forward");

        stepForwardButton.setOnMouseClicked(e -> stepForward());
        stepBackwardButton.setOnMouseClicked(e -> stepBackward());

        stepForwardButton.setDisable(true);
        stepBackwardButton.setDisable(true);

        probabilityChart = new ProbabilityChart(circuitController.widthProperty);
    }

    public double[] getMeasurements() {
        return measurements.get(breakpoints.get(breakpointIndex));
    }

    public void stepForward() {

        breakpointIndex++;
        probabilityChart.updateBarChart(measurements.get(0).length, probabilities.get(breakpoints.get(breakpointIndex)));
        updateButtons();

        circuitController.notifyStep();
    }

    public void stepBackward() {
        breakpointIndex--;
        probabilityChart.updateBarChart(measurements.get(0).length , probabilities.get(breakpoints.get(breakpointIndex)));
        updateButtons();

        circuitController.notifyStep();
    }

    public void update(Component[][] components, int numWires, int numGates) {

        probabilities.clear();
        measurements.clear();

        QuantumMachine quantumMachine = new QuantumMachine(numWires);

        for (int j = 0; j < numGates; j++) {
            for (int i = 0; i < numWires; i++) {
                if (components[i][j] != null) {

                    // Component is a gate
                    if (components[i][j].isGate()) {
                        StandardGate gate = (StandardGate) components[i][j];

                        // If controlled, the measured value must be 1
                        if (gate.isClassicallyControlled()) {
                            if (gate.getQuantumMeter().getClassicalBit() != 1)
                                continue;
                        }

                        // Add the gate to the machine
                        quantumMachine.addGate(gate.getGate());

                    } else {

                        // Component is a measurement
                        QuantumMeter quantumMeasure = (QuantumMeter) components[i][j];
                        // Measure quantum machine and set value
                        quantumMeasure.setClassicalBit(quantumMachine.measure(i));
                    }
                }
            }

            quantumMachine.execute();
            calculateProbabilitiesAndMeasurements(quantumMachine.getQubits(), numWires);

        }

        updateBreakpoints(numGates);
        probabilityChart.updateBarChart(numWires, probabilities.get(breakpoints.get(breakpointIndex)));

        circuitController.notifyIndicatorBarChange(Color.rgb(0, 200, 0, 0.5));
    }

    private void calculateProbabilitiesAndMeasurements(ComplexMatrix quantumState, int numWires) {

        int numResults = quantumState.rows;

        // Calculate probabilities and measurements after this 'step'
        double[] p = new double[numResults];
        double[] m = new double[numWires];

        for (int i = 0; i < numResults; i++) {
            p[i] = quantumState.data[i][0].getMagnitude2();
            for (int j = 0; j < numWires; j++) {
                if (((i >> (numWires - j - 1)) & 1) == 1) {
                    m[j] += p[i];
                }
            }
        };

        probabilities.add(p);
        measurements.add(m);
    }

    private void updateBreakpoints(int numGates) {

        // Remove breakpoints outside gate range
        breakpoints.removeIf(breakpoint -> breakpoint >= numGates);

        // Set breakpoint index within breakpoint range
        if (breakpointIndex >= breakpoints.size()) {
            breakpointIndex = breakpoints.size() - 1;
            stepForwardButton.setDisable(true);
        }

        // Check if no break points (add one at the end)
        if (!breakpoints.contains((Object) Integer.valueOf(numGates - 1)))
            breakpoints.add(numGates - 1);

        if (breakpointIndex < 0)
            breakpointIndex = 0;

        // Check buttons
        updateButtons();
    }

    private void updateButtons() {
        stepForwardButton.setDisable(breakpointIndex >= breakpoints.size() || breakpoints.size() == 1);
        stepBackwardButton.setDisable(breakpointIndex == 0);
    }

}
