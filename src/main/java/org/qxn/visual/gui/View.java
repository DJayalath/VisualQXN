package org.qxn.visual.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.qxn.gates.H;
import org.qxn.gates.X;
import org.qxn.gates.Y;
import org.qxn.gates.Z;

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
        rootPane.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
        stage.setTitle("VisualQXN");

        // pane for buttons
        GridPane topPane = new GridPane();
        rootPane.setTop(topPane);

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

        HBox runBox = new HBox();
        runBox.setAlignment(Pos.CENTER_LEFT);
        runBox.setSpacing(10);
        runBox.setPadding(new Insets(10));

        runBox.getChildren().addAll(run, circuit.getIndicator(), circuit.getNotification());

        HBox stepBox = new HBox();
        stepBox.setPadding(new Insets(10));
        stepBox.setSpacing(10);
        stepBox.setAlignment(Pos.CENTER_RIGHT);
        stepBox.getChildren().addAll(circuit.getStepBackward(), circuit.getStepForward());

        circuitPane.add(runBox, 0, 1);
        circuitPane.add(stepBox, 1, 1);

        circuitPane.setBorder(new Border(new BorderStroke(Color.LIGHTGRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT, Insets.EMPTY)));

        GridPane.setHalignment(stepBox, HPos.RIGHT);
//        circuitPane.setGridLinesVisible(true);

        // Side buttons
        VBox topV = new VBox();
        topV.setFillWidth(true);
        HBox topButtons = new HBox();
        topButtons.setAlignment(Pos.CENTER);
        topButtons.setSpacing(10);
        topButtons.setPadding(new Insets(10, 0, 10, 0));
        topV.getChildren().add(topButtons);

        Button bp = new Button("Break Point");
        bp.setOnMouseClicked(event -> circuit.toggleBreakPoint());

        Button addComponent = new Button("Component +");
        Button removeComponent = new Button("Component -");
        removeComponent.setOnMouseClicked(event -> circuit.removeComponent());
        Button connect = new Button("Connect");
        connect.setOnMouseClicked(event -> circuit.connect());

        topButtons.getChildren().addAll(addComponent, removeComponent, connect, bp, circuit.getAddWireButton(), circuit.getRemoveWireButton());

        rootPane.setTop(topV);
//        topPane.add(bp, 0, 0);

        // Status pane
        statusPane.add(circuit.getBarChart(), 0, 0);
        statusPane.setAlignment(Pos.CENTER);

        addComponent.setOnMouseClicked(event -> {
            Dialog<Component> dialog = new Dialog<>();
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Add Component");
            // No header

            // No icon

            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

            // Selection Fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            List<String> choices = new ArrayList<>();
            choices.add("H");
            choices.add("X");
            choices.add("Y");
            choices.add("Z");
            choices.add("SWAP");
            choices.add("CNOT");

            ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(choices));
            choiceBox.setValue(choices.get(0));

            Label gateLabel = new Label("Gate");

            Button addGate = new Button("+");
            addGate.setOnMouseClicked(e -> {
                // REMEMBER TO BREAK
                switch (choiceBox.getValue()) {
                    case "H":
                        dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new H(circuit.getSelectedRow())));
                        break;
                    case "X":
                        dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new X(circuit.getSelectedRow())));
                        break;
                    case "Y":
                        dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new Y(circuit.getSelectedRow())));
                        break;
                    case "Z":
                        dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new Z(circuit.getSelectedRow())));
                        break;
                    case "SWAP":
                        dialog.setResult(new SWAPGate(circuit.getSelectedRow(), circuit.getSelectedCol()));
                        break;
                    case "CNOT":
                        dialog.setResult(new CNOTGate(circuit.getSelectedRow(), circuit.getSelectedCol()));
                        break;
                    default:
                        break;
                }
            });

            grid.add(gateLabel, 0, 0);
            grid.add(choiceBox, 1, 0);
            grid.add(addGate, 2, 0);

            // Measurement
            Label measureLabel = new Label("Measure");
            Button addMeasure = new Button("+");
            addMeasure.setOnMouseClicked(e -> dialog.setResult(new QMeter(circuit.getSelectedRow(), circuit.getSelectedCol())));
            GridPane.setHalignment(measureLabel, HPos.CENTER);

            grid.add(measureLabel, 1, 1);
            grid.add(addMeasure, 2, 1);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> null);

            Optional<Component> result = dialog.showAndWait();
            if (result.isPresent()) {
                try {
                    circuit.addComponent(result.get());
                } catch (CircuitException circuitException) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Circuit Placement Error");
                    alert.setHeaderText("Error placing component");
                    alert.setContentText(circuitException.getMessage());

                    alert.showAndWait();
                }
            }

        });

        stage.show();

    }
}
