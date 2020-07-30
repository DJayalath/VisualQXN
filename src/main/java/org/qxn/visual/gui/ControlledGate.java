package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.C;

public class ControlledGate extends StandardGate {

    StandardGate original;

    public ControlledGate(StandardGate original) {
        super(original.getRow() - 1, original.getCol(), original.getSpan() + 1, new C(original.gate, original.getRow() - 1));
        this.row = original.getRow() - 1;
        this.col = original.getCol();
        this.span = original.getSpan() + 1;
        this.original = original;

        // Break connection
        if (original.getqMeter() != null)
            original.setqMeter(null);
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {

        // Draw control line
        double x = Circuit.getXFromCol(col) + Circuit.boxWidth / 2.0;
        double y = Circuit.getYFromRow(row) + Circuit.boxHeight / 2.0;

        double yEnd = y + Circuit.boxHeight + Circuit.rowDist;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(x - 5, y - 5, 10, 10);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd);

        // Draw gate
        original.draw(graphicsContext);

        // Draw classic control
        if (qMeter != null) {
            double targetY = Circuit.getYFromRow(qMeter.getRow()) + Circuit.boxHeight / 2.0;

            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.strokeLine(x - 2.5, y, x - 2.5, targetY);
            graphicsContext.strokeLine(x + 2.5, y, x + 2.5, targetY);
        }
    }
}
