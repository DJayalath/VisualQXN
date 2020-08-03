package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.List;

public class QuantumMeter extends Component {

    private int classicalBit = 0;

    public QuantumMeter() {
        super(1);
    }

    public int getClassicalBit() {
        return classicalBit;
    }

    public void setClassicalBit(int classicalBit) {
        this.classicalBit = classicalBit;
    }

    public void removeConnected(StandardGate gate) {
        classicallyControlledGates.remove(gate);
    }

    public void addConnected(StandardGate gate) {
        classicallyControlledGates.add(gate);
    }

    public void cleanUp() {
        for (StandardGate gate : classicallyControlledGates) {
            gate.removeQuantumMeter();
        }
    }

    private List<StandardGate> classicallyControlledGates;

    @Override
    public void draw(double x, double y, GraphicsContext graphicsContext) {

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(x, y, CircuitState.gateWidth, CircuitState.gateHeight);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeRect(x, y, CircuitState.gateWidth, CircuitState.gateHeight);

        graphicsContext.strokeArc(x + 5, y + CircuitState.gateHeight / 2.0, CircuitState.gateWidth - 10, 20, 0, 160, ArcType.OPEN);
        graphicsContext.strokeLine(x + CircuitState.gateWidth / 2.0 - 5 + 5, y + CircuitState.gateHeight / 2.0 + 10, x + CircuitState.gateWidth / 2.0 + 5 + 5, y + CircuitState.gateHeight / 2.0 - 10);

        // Classical line
        graphicsContext.strokeLine(x + CircuitState.gateWidth, y + CircuitState.gateHeight / 2.0 + 5, graphicsContext.getCanvas().getWidth(), y + CircuitState.gateHeight / 2.0 + 5);

    }
}
