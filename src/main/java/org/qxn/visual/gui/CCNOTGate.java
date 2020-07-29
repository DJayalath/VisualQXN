package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.CCNOT;

public class CCNOTGate extends StandardGate {
    public CCNOTGate(int row, int col) {
        super(row, col, 3, new CCNOT(row, row + 1, row + 2));
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        double x = Circuit.getXFromCol(col) + Circuit.boxWidth / 2.0;
        double y = Circuit.getYFromRow(row) + Circuit.boxHeight / 2.0;

        double yEnd = y + Circuit.boxHeight + Circuit.rowDist;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(x - 5, y - 5, 10, 10);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd + 10);

        y = Circuit.getYFromRow(row + 1) + Circuit.boxHeight / 2.0;
        yEnd = y + Circuit.boxHeight + Circuit.rowDist;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(x - 5, y - 5, 10, 10);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd + 10);

        graphicsContext.strokeOval(x - 10, yEnd - 10, 20, 20);
    }
}
