package org.qxn.visual.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.qxn.gates.Gate;

public class StandardGate extends Component {

    public Gate getGate() {
        return gate;
    }

    private final Gate gate;

    public StandardGate(int row, int col, int span, Gate gate) {
        super(row, col, span);
        this.gate = gate;
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
        graphicsContext.fillText(gate.getClass().getSimpleName(), x + width / 2.0, y + height / 2.0);

    }
}