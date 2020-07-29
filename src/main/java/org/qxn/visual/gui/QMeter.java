package org.qxn.visual.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

public class QMeter extends Component {

    public QMeter(int row, int col) {
        super(row, col, 1);
    }

    public int getClassicalBit() {
        return classicalBit;
    }

    public void setClassicalBit(int classicalBit) {
        this.classicalBit = classicalBit;
    }

    private int classicalBit = -1;

    public StandardGate getConnected() {
        return connected;
    }

    public void setConnected(StandardGate connected) {
        this.connected = connected;
    }

    private StandardGate connected;

    @Override
    public void draw(GraphicsContext graphicsContext) {

        double x = Circuit.getXFromCol(col);
        double y = Circuit.getYFromRow(row);

        double width = Circuit.boxWidth;
        double height = Circuit.boxHeight;

        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(x, y, width, height);
        graphicsContext.setStroke(Color.BLACK);
        graphicsContext.strokeRect(x, y, width, height);

        graphicsContext.strokeArc(x + 5, y + height / 2.0, width - 10, 20, 0, 160, ArcType.OPEN);
        graphicsContext.strokeLine(x + width / 2.0 - 5 + 5, y + height / 2.0 + 10, x + width / 2.0 + 5 + 5, y + height / 2.0 - 10);

        // Classical line
        graphicsContext.strokeLine(x + width, y + height / 2.0 + 5, Circuit.colDist + (Circuit.boxWidth + Circuit.colDist) * Circuit.maxGates, y + height / 2.0 + 5);

    }
}
