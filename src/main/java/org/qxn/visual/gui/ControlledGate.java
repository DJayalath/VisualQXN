package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.C;

public class ControlledGate extends StandardGate {

    private final StandardGate originalGate;

    public ControlledGate(StandardGate standardGate) {
        super(standardGate.label, new C(standardGate.getGate(), standardGate.getGate().getStartWire() - 1));
        this.originalGate = standardGate;
        this.span = originalGate.span + 1;
    }

    public void draw(double x, double y, GraphicsContext graphicsContext) {

        if (isClassicallyControlled())
            drawClassicalConnection(x, y, graphicsContext);

        // Draw control line
        double xControl = x + CircuitState.gateWidth / 2.0;
        double yControl = y + CircuitState.gateHeight / 2.0;

        double yEnd = yControl + CircuitState.gateHeight + CircuitState.wireGap;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(xControl - 5, yControl - 5, 10, 10);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(xControl, yControl, xControl, yEnd);

        y += CircuitState.gateHeight + CircuitState.wireGap;
        originalGate.drawGate(x, y, graphicsContext);
    }
}
