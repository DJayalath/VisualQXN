package org.qxn.visual.gui;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.qxn.gates.*;
import org.qxn.linalg.ComplexMatrix;

import java.io.File;
import java.io.IOException;
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
        stage.setResizable(false);
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
        Circuit circuit = new Circuit(4, Circuit.maxGates);
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
        GridPane topV = new GridPane();
        GridPane.setHgrow(topV, Priority.ALWAYS);
        topV.setPadding(new Insets(10));

        HBox topButtonsLeft = new HBox();
        topButtonsLeft.setAlignment(Pos.CENTER);
        topButtonsLeft.setSpacing(10);
        topV.add(topButtonsLeft, 0, 0);

        HBox topButtonsRight = new HBox();
        topButtonsRight.setAlignment(Pos.CENTER_RIGHT);
        topButtonsRight.setSpacing(10);
        topV.add(topButtonsRight, 1, 0);

        Button addComponent = new Button("Component +");
        Button removeComponent = new Button("Component -");
        removeComponent.setOnMouseClicked(event -> circuit.removeComponent());
        Button connect = new Button("Classical Control");
        connect.setOnMouseClicked(event -> circuit.connect());

        Button settings = new Button("Settings");
        Button clear = new Button("Clear");
        Button save = new Button("Save");

        save.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Circuit");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("VisualQXN Files", "*.vqxn"));
            fileChooser.setInitialFileName("*.vqxn");
            File file = fileChooser.showSaveDialog(stage);
            if (file != null) {
                try {
                    circuit.save(file.getAbsolutePath());
                } catch (IOException ioException) {
                    // Alert error
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Circuit File System Error");
                    alert.setHeaderText("Error saving circuit to file");
                    alert.setContentText("Failed to save circuit");

                    alert.showAndWait();
                }
            }
        });

        Button load = new Button("Load");
        load.setOnMouseClicked(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Circuit");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("VisualQXN Files", "*.vqxn"));
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    circuit.load(file.getAbsolutePath());
                    scene.getWindow().setWidth(circuit.getCanvas().getWidth());
                } catch (IOException | ClassNotFoundException ioException) {
                    // Alert error
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Circuit File System Error");
                    alert.setHeaderText("Error loading circuit from file");
                    alert.setContentText("Failed to load circuit");

                    alert.showAndWait();
                }
            }

        });

        GridPane.setHgrow(topButtonsRight, Priority.ALWAYS);
        topButtonsLeft.getChildren().addAll(addComponent, removeComponent, circuit.getAddWireButton(), circuit.getRemoveWireButton(), connect, circuit.getControlButton());
        topButtonsRight.getChildren().addAll(circuit.getUndoButton(), circuit.getRedoButton(), save, load, clear, settings);

        clear.setOnMouseClicked(event -> circuit.clear());

        settings.setOnMouseClicked(event -> {

            Dialog<String> dialog = new Dialog<>();
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Settings");
            // No header

            // No icon
            GridPane content = new GridPane();
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

            List<String> choices = new ArrayList<>();
            for (int i = 15; i <= 25; i += 5)
                choices.add(String.valueOf(i));

//            ChoiceDialog<String> dialog = new ChoiceDialog<>(String.valueOf(Circuit.maxGates), choices);
            ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(choices));
            choiceBox.setValue(String.valueOf(Circuit.maxGates));

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == ButtonType.OK) {
                    return choiceBox.getValue();
                }
                return null;
            });

            content.setVgap(5);
            content.setHgap(10);
            content.add(new Label("Wire length"), 0, 0);
            GridPane.setMargin(choiceBox, new Insets(10, 0, 10, 0));
            content.add(choiceBox, 1, 0);
            Label cLabel = new Label("Copyright (c) 2020 Dulhan Jayalath");
            content.add(cLabel, 0, 1, 2, 1);
            Hyperlink link = new Hyperlink("MIT LICENSE");

            link.setOnAction(e -> getHostServices().showDocument("https://github.com/armytricks/VisualQXN/blob/master/LICENSE"));
            content.add(link, 0, 2, 2, 1);

            GridPane.setHalignment(cLabel, HPos.CENTER);
            GridPane.setHalignment(link, HPos.CENTER);

            dialog.getDialogPane().setContent(content);

//            dialog.setTitle("Settings");
//            dialog.setHeaderText("Circuit Size");
//            dialog.setContentText("Wire length");

            // Traditional way to get the response value.
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                int newSize = Integer.parseInt(result.get());
                circuit.resize(newSize);
                scene.getWindow().setWidth(circuit.getCanvas().getWidth());
//                scene.getWindow().centerOnScreen();
            }
        });

        rootPane.setTop(topV);
//        topPane.add(bp, 0, 0);

        // Status pane
        GridPane.setVgrow(statusPane, Priority.ALWAYS);
        statusPane.add(circuit.getBarChart(), 0, 0);
        circuit.getBarChart().setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(circuit.getBarChart(), Priority.ALWAYS);
        statusPane.setAlignment(Pos.CENTER);
        GridPane.setHalignment(circuit.getBarChart(), HPos.CENTER);
        GridPane.setValignment(circuit.getBarChart(), VPos.CENTER);

        addComponent.setOnMouseClicked(event -> {
            Dialog<Component> dialog = new Dialog<>();
            dialog.initStyle(StageStyle.UTILITY);
            dialog.setTitle("Add Component");
            // No header

            // No icon

//            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            Window window = dialog.getDialogPane().getScene().getWindow();
            window.setOnCloseRequest(e -> window.hide());

            // Selection Fields
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPadding(new Insets(10, 10, 0, 10));

            List<String> choices = new ArrayList<>();
            choices.add("H");
            choices.add("S");
            choices.add("T");
            choices.add("X");
            choices.add("Y");
            choices.add("Z");
            choices.add("SWAP");
            choices.add("CSWAP");
            choices.add("CNOT");
            choices.add("CCNOT");

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
                    case "S":
                        dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new S(circuit.getSelectedRow())));
                        break;
                    case "T":
                        dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new T(circuit.getSelectedRow())));
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
                    case "CSWAP":
                        dialog.setResult(new CSWAPGate(circuit.getSelectedRow(), circuit.getSelectedCol()));
                        break;
                    case "CNOT":
                        dialog.setResult(new CNOTGate(circuit.getSelectedRow(), circuit.getSelectedCol()));
                        break;
                    case "CCNOT":
                        dialog.setResult(new CCNOTGate(circuit.getSelectedRow(), circuit.getSelectedCol()));
                        break;
                    default:
                        break;
                }
            });

            grid.add(gateLabel, 0, 0);
            grid.add(choiceBox, 1, 0);
            grid.add(addGate, 2, 0);

            // R Gate
            Label rLabel = new Label("R-Gate");
            TextField phi = new TextField();
            phi.setPromptText("\u03d5 (Rad.)");
            phi.setMinWidth(Double.MIN_VALUE);
            phi.setPrefWidth(Double.MIN_VALUE);
            Button addR = new Button("+");
            addR.setOnMouseClicked(e -> {
                double phaseShift = 0;
                try {
                    phaseShift = Double.parseDouble(phi.getText());
                } catch (Exception ignored) {}
                dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), 1, new R(circuit.getSelectedRow(), phaseShift)));
            });

            grid.add(rLabel, 0, 1);
            grid.add(phi, 1, 1);
            grid.add(addR, 2, 1);

            // QFT
            Label qftLabel = new Label("QFT-Gate");
            TextField qftN = new TextField();
            qftN.setPromptText("Size");
            qftN.setMinWidth(Double.MIN_VALUE);
            qftN.setPrefWidth(Double.MIN_VALUE);
            Button addQFT = new Button("+");
            addQFT.setOnMouseClicked(e -> {
                int size = 1;
                try {
                    size = Integer.parseInt(qftN.getText());
                } catch (Exception ignored) {}
                dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), size, new QFT(circuit.getSelectedRow(), size)));
            });

            grid.add(qftLabel, 0, 2);
            grid.add(qftN, 1, 2);
            grid.add(addQFT, 2, 2);

            // QFTHA
            Label qftHALabel = new Label("QFT\u2020-Gate");
            TextField qftNHA = new TextField();
            qftNHA.setPromptText("Size");
            qftNHA.setMinWidth(Double.MIN_VALUE);
            qftNHA.setPrefWidth(Double.MIN_VALUE);
            Button addQFTHA = new Button("+");
            addQFTHA.setOnMouseClicked(e -> {
                int size = 1;
                try {
                    size = Integer.parseInt(qftNHA.getText());
                } catch (Exception ignored) {}
                dialog.setResult(new StandardGate(circuit.getSelectedRow(), circuit.getSelectedCol(), size, new QFTHA(circuit.getSelectedRow(), size)));
            });

            grid.add(qftHALabel, 0, 3);
            grid.add(qftNHA, 1, 3);
            grid.add(addQFTHA, 2, 3);

            // Measurement
            Label measureLabel = new Label("Measure Qubit");
            Button addMeasure = new Button("+");
            addMeasure.setOnMouseClicked(e -> dialog.setResult(new QMeter(circuit.getSelectedRow(), circuit.getSelectedCol())));
            GridPane.setHalignment(measureLabel, HPos.LEFT);

            grid.add(measureLabel, 0, 4, 2, 1);
            grid.add(addMeasure, 2, 4);

            // Matrix
            Label matrixLabel = new Label("Matrix-Defined Gate");
            GridPane.setHalignment(matrixLabel, HPos.LEFT);
            Button addMatrix = new Button("+");
            addMatrix.setOnMouseClicked(e -> {

                grid.getChildren().clear();
                grid.setVgap(10);

                Label instructions = new Label("Use comma separated columns and new lines for rows");
                Label instructions2 = new Label("Write values in component notation with (x:y) = x + iy");
                Label example = new Label("(1:0), (0:0)\n(0:0), (0:1)");
                Label eval = new Label("evaluates to");
                Label example2 = new Label("(1 + 0i, 0 + 0i)\n(0 + 0i, 0 + 1i)");
                HBox hbox = new HBox();
                hbox.setSpacing(10);
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().addAll(example, eval, example2);
                TextArea matrixInput = new TextArea();
                matrixInput.setMinWidth(Double.MIN_VALUE);
                matrixInput.setPrefWidth(Double.MIN_VALUE);
                grid.add(instructions, 0, 0, 3, 1);
                grid.add(instructions2, 0, 1, 3, 1);
                grid.add(hbox, 0, 2, 3, 1);

                TextField label = new TextField();
                label.setPromptText("Enter gate label");

                grid.add(label, 0, 3);

                grid.add(matrixInput, 0, 4, 3, 1);

                Label errorLabel = new Label("");
                errorLabel.setTextFill(Color.RED);

                Button add = new Button("+");
                add.setOnMouseClicked(ev -> {
                    String[] rows = matrixInput.getText().split("\n");
                    ComplexMatrix gateMatrix = new ComplexMatrix(rows.length, rows.length);

                    try {
                        int i = 0;
                        for (String row : rows) {
                            int j = 0;
                            String[] vals = row.split(",");
                            for (String val : vals) {
                                val = val.replace("(", "");
                                val = val.replace(")", "");
                                val = val.replace(" ", "");
                                String[] realImaginary = val.split(":");

                                gateMatrix.data[i][j].real = Double.parseDouble(realImaginary[0]);
                                gateMatrix.data[i][j].imaginary = Double.parseDouble(realImaginary[1]);

                                j++;
                            }
                            i++;
                        }
                    } catch (Exception exception) {
                        errorLabel.setText("Invalid matrix entry format");
                        return;
                    }

                    // Check unitary
                    if (!gateMatrix.isUnitary()) {
                        errorLabel.setText("Input matrix is not unitary");
                        return;
                    }

                    int span = (int) (Math.log(rows.length) / Math.log(2));
                    dialog.setResult(new MatrixGate(circuit.getSelectedRow(), circuit.getSelectedCol(), span, new CustomGate(circuit.getSelectedRow(), span, gateMatrix), label.getText()));
                });

                grid.add(errorLabel, 0, 5, 2, 1);
                grid.add(add, 2, 5);
                GridPane.setHalignment(add, HPos.RIGHT);

                dialog.getDialogPane().getScene().getWindow().sizeToScene();
                dialog.getDialogPane().getScene().getWindow().centerOnScreen();

            });

            grid.add(matrixLabel, 0, 5, 2, 1);
            grid.add(addMatrix, 2, 5);

            // Functional Oracle
            Label functionOracleLabel = new Label("Function-Defined Gate");
            GridPane.setHalignment(functionOracleLabel, HPos.LEFT);
            Button addOracle = new Button("+");
            addOracle.setOnMouseClicked(e -> {
                grid.getChildren().clear();
                grid.setVgap(10);

                Label instructions = new Label("Defining U : (x, y) \u2192 (x, y \u2295 f(x))\n\nf(x) = 1 when the inequality is true\nf(x) = 0 otherwise");
                TextField label = new TextField();
                label.setPromptText("Enter oracle label");

                List<String> gateSize = new ArrayList<>();
                for (int i = 2; i <= 10; i++)
                    gateSize.add(String.valueOf(i));

                Label gateSizeLabel = new Label ("Gate Size");
                ChoiceBox<String> gateChoice = new ChoiceBox<>(FXCollections.observableArrayList(gateSize));
                gateChoice.setValue(gateSize.get(0));

                List<String> iChoices = new ArrayList<>();

                iChoices.add(">");
                iChoices.add("<");
                iChoices.add(">=");
                iChoices.add("<=");
                iChoices.add("=");
                iChoices.add("!=");
                iChoices.add("%");

                ChoiceBox<String> inequalities = new ChoiceBox<>(FXCollections.observableArrayList(iChoices));
                inequalities.setValue(iChoices.get(0));

                Label xLabel = new Label("X");

                TextField entry = new TextField();

                Label errorLabel = new Label("");
                errorLabel.setTextFill(Color.RED);

                Button add = new Button("+");
                GridPane.setHalignment(add, HPos.RIGHT);
                add.setOnMouseClicked(f -> {
                    try {
                        final int number = Integer.parseInt(entry.getText());
                        Oracle.BitStringMap bitStringMap = new Oracle.BitStringMap() {
                            final String inequality = inequalities.getValue();
                            @Override
                            public boolean test(int i) {
                                switch (inequality) {
                                    case ">":
                                        return i > number;
                                    case "<":
                                        return i < number;
                                    case ">=":
                                        return i >= number;
                                    case "<=":
                                        return i <= number;
                                    case "=":
                                        return i == number;
                                    case "!=":
                                        return i != number;
                                    case "%":
                                        return i % number == 0;
                                    default:
                                        return false;
                                }
                            }
                        };

                        int gateSpan = Integer.parseInt(gateChoice.getValue());
                        dialog.setResult(
                                new MatrixGate(
                                        circuit.getSelectedRow(), circuit.getSelectedCol(), gateSpan,
                                        new Oracle(circuit.getSelectedRow(), gateSpan - 1, circuit.getSelectedRow() + gateSpan, bitStringMap),
                                        label.getText()
                                )
                        );
                    } catch (NumberFormatException numberFormatException) {
                        errorLabel.setText("Invalid number input");
                    }
                });

                grid.add(new Label("Oracle label"), 0, 0);
                grid.add(label, 1, 0);
                grid.add(gateSizeLabel, 0, 1);
                GridPane.setHalignment(gateChoice, HPos.RIGHT);
                grid.add(gateChoice, 1, 1);
                grid.add(instructions, 0, 2, 2, 1);
                grid.add(errorLabel, 0, 4);
                grid.add(add, 1, 4);

                GridPane iqPane = new GridPane();
                GridPane.setHalignment(iqPane, HPos.RIGHT);
                iqPane.setHgap(5);
                grid.add(iqPane, 0, 3, 2, 1);

                iqPane.add(xLabel, 0, 0);
                iqPane.add(inequalities, 1, 0);
                GridPane.setHalignment(entry, HPos.RIGHT);
                iqPane.add(entry, 2, 0);

                dialog.getDialogPane().getScene().getWindow().sizeToScene();
                dialog.getDialogPane().getScene().getWindow().centerOnScreen();
            });

            grid.add(functionOracleLabel, 0, 6, 2, 1);
            grid.add(addOracle, 2, 6);

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
