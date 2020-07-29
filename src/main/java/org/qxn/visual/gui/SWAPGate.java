package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.SWAP;

public class SWAPGate extends StandardGate {
    public SWAPGate(int row, int col) {
        super(row, col, 2, new SWAP(row, row + 1));
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {
        double x = Circuit.getXFromCol(col) + Circuit.boxWidth / 2.0;
        double y = Circuit.getYFromRow(row) + Circuit.boxHeight / 2.0;

        double yEnd = y + Circuit.boxHeight + Circuit.rowDist;

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd);

        graphicsContext.strokeLine(x - 5, y - 5, x + 5, y + 5);
        graphicsContext.strokeLine(x + 5, y - 5, x - 5, y + 5);

        graphicsContext.strokeLine(x - 5, yEnd - 5, x + 5, yEnd + 5);
        graphicsContext.strokeLine(x + 5, yEnd - 5, x - 5, yEnd + 5);
    }
}
