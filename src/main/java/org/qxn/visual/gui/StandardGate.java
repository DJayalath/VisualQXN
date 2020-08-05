package org.qxn.visual.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.qxn.gates.Gate;

public class StandardGate extends Component {

    private final Gate gate;
    protected String label;

    private QuantumMeter quantumMeter;

    public Gate getGate() {
        return gate;
    }

    public boolean isClassicallyControlled() {
        return quantumMeter != null;
    }

    public void setQuantumMeter(QuantumMeter quantumMeter) {
        this.quantumMeter = quantumMeter;
        quantumMeter.addConnected(this);
    }

    public void removeQuantumMeter() {
        this.quantumMeter = null;
    }

    public QuantumMeter getQuantumMeter() {
        return quantumMeter;
    }

    public void cleanUp() {
        if (isClassicallyControlled()) {
            quantumMeter.removeConnected(this);
        }
    }

    public StandardGate(String label, Gate gate) {
        super(gate.getNumInputs());
        this.gate = gate;
        this.label = label;
        isGate = true;
    }

    protected void drawClassicalConnection(double x, double y, GraphicsContext graphicsContext) {

        graphicsContext.setStroke(Color.BLACK);
        double targetY = CircuitState.getYFromRow(quantumMeter.getRow()) + CircuitState.gateHeight / 2.0;
        x += CircuitState.gateWidth / 2.0;
        graphicsContext.strokeLine(x - 2.5, y, x - 2.5, targetY);
        graphicsContext.strokeLine(x + 2.5, y, x + 2.5, targetY);
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(x - 3, targetY + 2.5 - 3, 6, 6);
    }

    @Override
    public void draw(double x, double y, GraphicsContext graphicsContext) {

        if (isClassicallyControlled())
            drawClassicalConnection(x, y, graphicsContext);

        drawGate(x, y, graphicsContext);

    }

    protected void drawGate(double x, double y, GraphicsContext graphicsContext) {
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.setStroke(Color.BLACK);

        graphicsContext.fillRect(x, y, CircuitState.gateWidth, CircuitState.gateHeight);
        graphicsContext.strokeRect(x, y, CircuitState.gateWidth, CircuitState.gateHeight);

        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(VPos.CENTER);
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillText(label, x + CircuitState.gateWidth / 2.0, y + CircuitState.gateHeight / 2.0);
    }
}
