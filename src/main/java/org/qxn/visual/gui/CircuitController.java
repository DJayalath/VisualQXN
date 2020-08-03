package org.qxn.visual.gui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class CircuitController {

    private final Executor executor;
    private final CircuitState circuitState;

    public Canvas getCanvas() {
        return canvas;
    }

    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private final Rectangle indicatorBackground;
    private final Label indicatorLabel;

    public final ObservableValue<? extends Number> widthProperty;

    public CircuitController(GridPane circuitPane) {
        widthProperty = circuitPane.widthProperty();

        executor = new Executor(this);

        StackPane indicatorStack = new StackPane();
        indicatorStack.setPrefHeight(30);
        indicatorStack.prefWidthProperty().bind(circuitPane.widthProperty());

        indicatorBackground = new Rectangle();
        indicatorBackground.widthProperty().bind(indicatorStack.widthProperty());
        indicatorBackground.heightProperty().bind(indicatorStack.heightProperty());
        indicatorBackground.setFill(Color.ORANGE);
        indicatorLabel = new Label("We good lads");
        indicatorStack.getChildren().add(indicatorBackground);
        indicatorStack.getChildren().add(indicatorLabel);
        StackPane.setAlignment(indicatorLabel, Pos.CENTER);

        canvas = new Canvas();
        graphicsContext = canvas.getGraphicsContext2D();
        circuitState = new CircuitState(2, 15, canvas, this);

        HBox stepButtonBox = new HBox(5, executor.getStepForwardButton(), executor.getStepBackwardButton(),
                circuitState.getAddWireButton(), circuitState.getRemoveWireButton(), new Label("Gate"), circuitState.getGateSelect());
        stepButtonBox.setPadding(new Insets(10));
        GridPane.setVgrow(stepButtonBox, Priority.ALWAYS);
        stepButtonBox.setAlignment(Pos.CENTER_LEFT);

        GridPane.setVgrow(executor.getProbabilityChart().getBarChart(), Priority.ALWAYS);

        circuitPane.add(indicatorStack, 0, 0);
        circuitPane.add(canvas, 0, 1);
        circuitPane.add(stepButtonBox, 0, 2);
        circuitPane.add(executor.getProbabilityChart().getBarChart(), 0, 3);

        notifyCanvasChange();
    }

    public void notifyCanvasChange() {
        circuitState.draw(graphicsContext);
    }

    // Draw measurements
    public void notifyMeasurementsChange() {
        double[] percentages = executor.getMeasurements();
    }

    // Draw indicator bar
    public void notifyIndicatorBarChange(Paint paint) {
        indicatorBackground.setFill(paint);
    }

    // Set text
    public void notifyIndicatorLabelChange(String message) {
        indicatorLabel.setText(message);
    }

}
