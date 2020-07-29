package org.qxn.visual.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.qxn.gates.CNOT;

public class CNOTGate extends StandardGate {

    public CNOTGate(int row, int col) {
        super(row, col, 2, new CNOT(row, row + 1));
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

        graphicsContext.strokeOval(x - 10, yEnd - 10, 20, 20);
    }
}
