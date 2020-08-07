package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.SWAP;

public class SWAPGate extends StandardGate {
    public SWAPGate(int row) {
        super("SWAP", new SWAP(row, row + 1));
        this.span = 2;
    }

    @Override
    public void draw(double x, double y, GraphicsContext graphicsContext) {

        if (isClassicallyControlled())
            drawClassicalConnection(x, y, graphicsContext);

        drawGate(x, y, graphicsContext);
    }

    @Override
    protected void drawGate(double x, double y, GraphicsContext graphicsContext) {

        x += CircuitState.gateWidth / 2.0;
        y += CircuitState.gateHeight / 2.0;

        double yEnd = y + CircuitState.gateHeight + CircuitState.wireGap;

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd);

        graphicsContext.strokeLine(x - 5, y - 5, x + 5, y + 5);
        graphicsContext.strokeLine(x + 5, y - 5, x - 5, y + 5);

        graphicsContext.strokeLine(x - 5, yEnd - 5, x + 5, yEnd + 5);
        graphicsContext.strokeLine(x + 5, yEnd - 5, x - 5, yEnd + 5);

    }
}
