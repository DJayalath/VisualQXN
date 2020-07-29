package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.qxn.gates.CSWAP;
import org.qxn.gates.SWAP;

public class CSWAPGate extends StandardGate {
    public CSWAPGate(int row, int col) {
        super(row, col, 3, new CSWAP(new SWAP(row + 1, row + 2), row));
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {

        double x = Circuit.getXFromCol(col) + Circuit.boxWidth / 2.0;
        double y = Circuit.getYFromRow(row) + Circuit.boxHeight / 2.0;

        double yEnd = y + Circuit.boxHeight + Circuit.rowDist;

        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillOval(x - 5, y - 5, 10, 10);

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd);

        y = Circuit.getYFromRow(row + 1) + Circuit.boxHeight / 2.0;
        yEnd = y + Circuit.boxHeight + Circuit.rowDist;

        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeLine(x, y, x, yEnd);

        graphicsContext.strokeLine(x - 5, y - 5, x + 5, y + 5);
        graphicsContext.strokeLine(x + 5, y - 5, x - 5, y + 5);

        graphicsContext.strokeLine(x - 5, yEnd - 5, x + 5, yEnd + 5);
        graphicsContext.strokeLine(x + 5, yEnd - 5, x - 5, yEnd + 5);
    }
}
