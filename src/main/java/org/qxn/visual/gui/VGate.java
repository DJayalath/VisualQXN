package org.qxn.visual.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class VGate{

    public String getLabel() {
        return label;
    }

    private String label;

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getSpan() {
        return span;
    }

    private final int gridX;
    private final int gridY;
    private final int span;

    public VGate getConnected() {
        return connected;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    private int value = -1;


    private VGate connected;

    public VGate(String label, int gridX, int gridY, int span) {
        this.label = label;
        this.gridX = gridX;
        this.gridY = gridY;
        this.span = span;
    }

    public void setConnected(VGate vGate) {
        connected = vGate;
    }

    public void draw(GraphicsContext graphicsContext) {

        double gateHeight = 40;
        double gateWidth = 40;

        double xx = CircuitController.LEFT_PADDING + gridX * CircuitController.GRID_GAP;
        double yy = CircuitController.TOP_PADDING + gridY * CircuitController.LINE_GAP;
        double x0 = CircuitController.LEFT_PADDING + gridX * CircuitController.GRID_GAP - gateWidth / 2.0;
        double y0 = CircuitController.TOP_PADDING + gridY * CircuitController.LINE_GAP - gateHeight / 2.0;

        double height = gateWidth + (span - 1) * CircuitController.LINE_GAP;
        double width = gateHeight;

        switch (label) {
            case "M":

                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.setFont(new Font("Arial", 32 - (label.length() - 1) * (7 - span)));
                graphicsContext.setTextAlign(TextAlignment.CENTER);
                graphicsContext.setTextBaseline(VPos.CENTER);


                graphicsContext.fillRect(x0, y0, width, height);
                graphicsContext.strokeRect(x0, y0, width, height);

                graphicsContext.strokeArc(x0 + 5, y0 + gateHeight / 2.0, gateWidth - 10, 20, 0, 160, ArcType.OPEN);
                graphicsContext.strokeLine(x0 + gateWidth / 2 - 5 + 5, y0 + gateHeight / 2 + 10, x0 + gateWidth / 2 + 5 + 5, y0 + gateWidth / 2 - 10);
                break;
            case "CNOT":
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.fillOval(xx - 5, yy - 5, 10, 10);
                graphicsContext.strokeLine(xx, yy, xx, yy + CircuitController.LINE_GAP + 10);
                graphicsContext.strokeOval(xx - 10, yy - 10 + CircuitController.LINE_GAP, 20, 20);
                break;
            case "SWAP":
                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.strokeLine(xx - 10, yy - 10, xx + 10, yy + 10);
                graphicsContext.strokeLine(xx + 10, yy - 10, xx - 10, yy + 10);
                graphicsContext.strokeLine(xx, yy, xx, yy + CircuitController.LINE_GAP);
                graphicsContext.strokeLine(xx - 10, yy + CircuitController.LINE_GAP - 10, xx + 10, yy + CircuitController.LINE_GAP + 10);
                graphicsContext.strokeLine(xx + 10, yy + CircuitController.LINE_GAP - 10, xx - 10, yy + CircuitController.LINE_GAP + 10);
                break;
            default:

                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setFill(Color.WHITE);
                graphicsContext.setFont(new Font("Arial", 32 - (label.length() - 1) * (7 - span)));
                graphicsContext.setTextAlign(TextAlignment.CENTER);
                graphicsContext.setTextBaseline(VPos.CENTER);


                graphicsContext.fillRect(x0, y0, width, height);
                graphicsContext.strokeRect(x0, y0, width, height);
                graphicsContext.setFill(Color.BLACK);
                graphicsContext.fillText(label, x0 + width / 2.0, y0 + height / 2.0, width - 5.0);
                break;
        }

        // Draw connection
        if (connected != null) {

            int connectX = connected.getGridX();
            int connectY = connected.getGridY();

            double xStart = CircuitController.LEFT_PADDING + connectX * CircuitController.GRID_GAP + gateWidth / 2.0;
            double w = Math.abs(connectX - gridX) * CircuitController.GRID_GAP - gateWidth / 2.0;

            graphicsContext.strokeRect(xStart, CircuitController.TOP_PADDING + connectY * CircuitController.LINE_GAP - 4, w, 4);

            double xEnd = CircuitController.LEFT_PADDING + gridX * CircuitController.GRID_GAP;
            double h = Math.abs(connectY - gridY) * CircuitController.LINE_GAP - gateHeight / 2.0 + 4;
            graphicsContext.strokeRect(xEnd, CircuitController.TOP_PADDING + connectY * CircuitController.LINE_GAP - 4, 4, h);

//            double xStart = CircuitController.LEFT_PADDING + gridX * CircuitController.GRID_GAP;
//            double xEnd = CircuitController.LEFT_PADDING + connectX * CircuitController.GRID_GAP;
//
//            // ONLY APPLYABLE FROM ABOVE AND BEFORE!
//
//            // It is above
//            if (connectY < gridY) {
//                graphicsContext.strokeRect(xEnd, CircuitController.TOP_PADDING + ((gridY + connectY) / 2.0) * CircuitController.LINE_GAP, Math.abs(gridY - connectY) * CircuitController.LINE_GAP, 4);
//
//
//                double yStart = CircuitController.TOP_PADDING + connectY * CircuitController.LINE_GAP + gateHeight / 2.0;
//                double h = CircuitController.LINE_GAP / 2.0 - gateHeight / 2.0;
//                graphicsContext.strokeRect(xEnd, yStart, 4, h);
//                double yMid = CircuitController.TOP_PADDING + (connectY + 0.5) * CircuitController.LINE_GAP;
//                graphicsContext.strokeRect(xStart, yMid, 4, h);
//            }
        }

    }

}
