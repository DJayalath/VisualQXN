package org.qxn.visual.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.qxn.gates.Gate;

public class MatrixGate extends StandardGate {

    protected String label;

    public MatrixGate(int row, int col, int span, Gate gate, String label) {
        super(row, col, span, gate);
        this.label = label;
    }

    @Override
    public void draw(GraphicsContext graphicsContext) {

        double x = Circuit.getXFromCol(col);
        double y = Circuit.getYFromRow(row);

        double width = Circuit.boxWidth;
        double height = (Circuit.boxHeight) + (Circuit.boxHeight + Circuit.rowDist) * (span - 1);

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeRect(x, y, width, height);

        graphicsContext.setTextAlign(TextAlignment.CENTER);
        graphicsContext.setTextBaseline(VPos.CENTER);
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillText(label, x + width / 2.0, y + height / 2.0);

        // Draw connection
        if (qMeter != null) {
            double targetY = Circuit.getYFromRow(qMeter.getRow()) + Circuit.boxHeight / 2.0;

            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.strokeLine(x + Circuit.boxWidth / 2.0 - 2.5, y, x + Circuit.boxWidth / 2.0 - 2.5, targetY);
            graphicsContext.strokeLine(x + Circuit.boxWidth / 2.0 + 2.5, y, x + Circuit.boxWidth / 2.0 + 2.5, targetY);
        }

    }
}
