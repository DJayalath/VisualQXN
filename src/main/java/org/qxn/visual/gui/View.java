package org.qxn.visual.gui;

import javafx.application.Application;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import org.qxn.linalg.Complex;
import org.qxn.linalg.ComplexMath;
import org.qxn.linalg.ComplexMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class View extends Application {

    public static void initialise(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        BorderPane rootPane = new BorderPane();

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("VisualQXN");

        // Side pane for buttons
        GridPane leftPane = new GridPane();
        rootPane.setLeft(leftPane);

        // Centre pane
        GridPane centerPane = new GridPane();
        rootPane.setCenter(centerPane);

        // Grid for circuit
        GridPane circuitPane = new GridPane();
        centerPane.add(circuitPane, 0, 0);

        // Grid for status
        GridPane statusPane = new GridPane();
        centerPane.add(statusPane, 0, 1);

        // Circuit
        circuitPane.setAlignment(Pos.CENTER);
        Circuit circuit = new Circuit(4);
        circuit.draw();
        circuit.getCanvas().setOnMouseClicked(event -> circuit.select(event.getX(), event.getY()));
        circuitPane.add(circuit.getCanvas(), 0, 0, 2, 1);

        Button run = new Button("RUN");
        run.setOnMouseClicked(event -> circuit.run());
        Button bp = new Button("BP");
        bp.setOnMouseClicked(event -> circuit.addBreakPoint());

        HBox runBox = new HBox();
        runBox.setAlignment(Pos.CENTER_LEFT);
        runBox.setSpacing(10);
        runBox.setPadding(new Insets(10));

        runBox.getChildren().addAll(run, circuit.getIndicator());

        HBox stepBox = new HBox();
        stepBox.setPadding(new Insets(10));
        stepBox.setSpacing(10);
        stepBox.setAlignment(Pos.CENTER_RIGHT);
        stepBox.getChildren().addAll(circuit.getStepBackward(), circuit.getStepForward());

        circuitPane.add(runBox, 0, 1);
        circuitPane.add(stepBox, 1, 1);

        GridPane.setHalignment(stepBox, HPos.RIGHT);
        circuitPane.setGridLinesVisible(true);

        // Side buttons
        leftPane.add(bp, 0, 0);

        // Status pane
        statusPane.add(circuit.getBarChart(), 0, 0);
        statusPane.setAlignment(Pos.CENTER);


        stage.show();

    }
}
