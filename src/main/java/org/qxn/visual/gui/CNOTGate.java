package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.CNOT;

public class CNOTGate extends StandardGate {
    public CNOTGate(int row) {
        super("CNOT", new CNOT(row, row + 1));
    }

    @Override
    public void draw(double x, double y, GraphicsContext graphicsContext) {

        if (isClassicallyControlled())
            drawClassicalConnection(x, y, graphicsContext);

        x += CircuitState.gateWidth / 2.0;
        y += CircuitState.gateHeight / 2.0;

        double yEnd = y + CircuitState.gateHeight + CircuitState.wireGap;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(x - 5, y - 5, 10, 10);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd + 10);

        graphicsContext.strokeOval(x - 10, yEnd - 10, 20, 20);

    }
}
