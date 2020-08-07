package org.qxn.visual.gui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

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
        indicatorLabel.setTextAlignment(TextAlignment.CENTER);
//        GridPane.setHgrow(indicatorLabel, Priority.ALWAYS);
//        GridPane.setHalignment(indicatorLabel, HPos.CENTER);

        Button settingsButton = new Button("Settings");
        settingsButton.getStyleClass().add("indicator-button");
        GridPane.setHgrow(settingsButton, Priority.ALWAYS);
        GridPane.setHalignment(settingsButton, HPos.RIGHT);

        HBox indicatorLeftRow = new HBox();
        indicatorLeftRow.setSpacing(5);
        GridPane.setHgrow(indicatorLeftRow, Priority.ALWAYS);
        GridPane.setHalignment(indicatorLeftRow, HPos.LEFT);

        Button undoButton = new Button("UNDO");
        undoButton.getStyleClass().add("indicator-button");
        Button redoButton = new Button("REDO");
        redoButton.getStyleClass().add("indicator-button");
        Button refreshButton = new Button("REFRESH");
        refreshButton.getStyleClass().add("indicator-button");
        indicatorLeftRow.getChildren().addAll(undoButton, redoButton, refreshButton);

        GridPane indicatorRow = new GridPane();
        indicatorRow.setPadding(new Insets(5, 10, 5, 10));
        indicatorRow.setAlignment(Pos.CENTER_LEFT);
        indicatorRow.prefWidthProperty().bind(indicatorStack.widthProperty());
        indicatorRow.prefHeightProperty().bind(indicatorStack.heightProperty());

        indicatorRow.add(indicatorLeftRow, 0, 0);
        indicatorRow.add(settingsButton, 1, 0);

        indicatorStack.getChildren().add(indicatorBackground);
        indicatorStack.getChildren().add(indicatorRow);
        indicatorStack.getChildren().add(indicatorLabel);
        StackPane.setAlignment(indicatorRow, Pos.CENTER);
        StackPane.setAlignment(indicatorLabel, Pos.CENTER);

        canvas = new Canvas();
        graphicsContext = canvas.getGraphicsContext2D();
        circuitState = new CircuitState(2, 15, canvas, this);

        HBox stepButtonBox = new HBox(5, executor.getStepForwardButton(), executor.getStepBackwardButton(),
                circuitState.getAddWireButton(), circuitState.getRemoveWireButton());
        stepButtonBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(stepButtonBox, Priority.ALWAYS);

        HBox gateButtonBox = new HBox(5, new Label("Component"), circuitState.getGateSelect(),
                circuitState.getControlButton(), circuitState.getRemoveComponentButton());
        HBox.setHgrow(gateButtonBox, Priority.ALWAYS);
        gateButtonBox.setAlignment(Pos.CENTER_RIGHT);

        HBox buttonBox = new HBox(stepButtonBox, gateButtonBox);
        buttonBox.setPadding(new Insets(10));
        GridPane.setVgrow(buttonBox, Priority.ALWAYS);

        GridPane.setVgrow(executor.getProbabilityChart().getBarChart(), Priority.ALWAYS);

        circuitPane.add(indicatorStack, 0, 0);
        circuitPane.add(canvas, 0, 1);
        circuitPane.add(buttonBox, 0, 2);
        circuitPane.add(executor.getProbabilityChart().getBarChart(), 0, 3);

        refreshButton.setOnMouseClicked(e -> notifyCircuitStateChange());

        notifyCircuitChange();
    }

    public void notifyCircuitChange() {
        circuitState.draw(graphicsContext);
    }

    public void notifyCircuitStateChange() {
        notifyCircuitChange();
        executor.update(circuitState.getComponents(), circuitState.getNumWires(), circuitState.getNumGates());
    }

    // Draw measurements
    public void notifyMeasurementsChange() {
        double[] percentages = executor.getMeasurements();
        circuitState.setMeasurements(percentages);
        notifyCircuitChange();
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
