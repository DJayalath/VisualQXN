package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
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

    @Override
    public void draw(double x, double y, GraphicsContext graphicsContext) {

    }
}
