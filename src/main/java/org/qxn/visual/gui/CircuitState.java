package org.qxn.visual.gui;

import javafx.collections.FXCollections;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.qxn.gates.*;
import org.qxn.linalg.ComplexMatrix;
;import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class CircuitState {

    public static final int maxWires = 10;
    public static final int maxGates = 20;

    public static final double wireGap = 20;
    public static final double gateGap = 20;

    public static final double gateWidth = 50;
    public static final double gateHeight = 50;
    private final Button addWireButton;
    private final Button removeWireButton;
    private final Button removeComponentButton;

    public Button getControlButton() {
        return controlButton;
    }

    private final Button controlButton;

    public Button getClassicalControlButton() {
        return classicalControlButton;
    }

    private final Button classicalControlButton;

    private final ChoiceBox<String> gateSelect;
    private final Canvas canvas;
    private final CircuitController circuitController;

    public Component[][] getComponents() {
        return components;
    }

    private Component[][] components;

    public int getNumWires() {
        return numWires;
    }

    public int getNumGates() {
        return numGates;
    }

    private int numWires;
    private int numGates;
    private int hoverRow, hoverCol;
    private boolean hoverEnabled = false;
    private int selectedRow, selectedCol, selectedSpan;
    private boolean selectedEnabled = false;

    public CircuitState(int numWires, int numGates, Canvas canvas, CircuitController circuitController) {

        this.circuitController = circuitController;

        this.numWires = numWires;
        this.numGates = numGates;

        this.canvas = canvas;
        canvas.setOnMouseEntered(e -> hoverEnabled = true);
        canvas.setOnMouseExited(e -> hoverEnabled = false);
        canvas.setOnMouseMoved(e -> hover(e.getX(), e.getY()));
        canvas.setOnMouseClicked(e -> select(e.getX(), e.getY()));
        resizeCanvas();

        components = new Component[numWires][numGates];

        addWireButton = new Button("Wire +");
        removeWireButton = new Button("Wire -");
        removeComponentButton = new Button("DELETE");
        removeComponentButton.setDisable(true);
        controlButton = new Button("Control");
        controlButton.setDisable(true);
        classicalControlButton = new Button("Classic Control");
        classicalControlButton.setDisable(true);

        addWireButton.setOnMouseClicked(e -> addWire());
        removeWireButton.setOnMouseClicked(e -> removeWire());
        removeComponentButton.setOnMouseClicked(e -> removeComponent(selectedRow, selectedCol));
        controlButton.setOnMouseClicked(e -> control(selectedRow, selectedCol));
        classicalControlButton.setOnMouseClicked(e -> initiateClassicControl(selectedRow, selectedCol));

        List<String> gates = new ArrayList<>();
        gates.add("H");
        gates.add("X");
        gates.add("Y");
        gates.add("Z");
        gates.add("CNOT");
        gates.add("SWAP");
        gates.add("Measure");
        gates.add("R");
        gates.add("S");
        gates.add("T");
        gates.add("QFT");
        gates.add("Custom (Matrix)");
        gateSelect = new ChoiceBox<>(FXCollections.observableArrayList(gates));
        gateSelect.setValue(gates.get(0));

        updateButtons();
    }

    public Button getAddWireButton() {
        return addWireButton;
    }

    public Button getRemoveWireButton() {
        return removeWireButton;
    }

    public Button getRemoveComponentButton() {
        return removeComponentButton;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public ChoiceBox<String> getGateSelect() {
        return gateSelect;
    }

    private void updateButtons() {
        addWireButton.setDisable(numWires >= maxWires);
        removeWireButton.setDisable(numWires <= 1);
    }

    public void setNumGates(int numGates) {

        Component[][] temp = new Component[numWires][numGates];

        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < Math.min(this.numGates, numGates); j++) {
                temp[i][j] = components[i][j];
            }
        }

        components = temp;

        this.numGates = numGates;
        resizeCanvas();
        circuitController.notifyState();
    }

    private void addWire() {
        setNumWires(numWires + 1);
    }

    private void removeWire() {

        // Remove exposed components
        for (int j = 0; j < numGates; j++) {
            Integer r = getComponentRow(numWires - 1, j);
            if (r != null) {
                components[r][j] = null;
            }
        }

        setNumWires(numWires - 1);
    }

    private void setNumWires(int numWires) {

        Component[][] temp = new Component[numWires][numGates];

        for (int i = 0; i < Math.min(this.numWires, numWires); i++) {
            for (int j = 0; j < numGates; j++) {
                temp[i][j] = components[i][j];
            }
        }

        components = temp;

        this.numWires = numWires;
        resizeCanvas();
        updateButtons();

        circuitController.notifyState();
    }

    private void resizeCanvas() {
        canvas.setWidth( (gateGap + gateWidth) * numGates + gateGap );
        canvas.setHeight( (wireGap + gateHeight) * numWires + wireGap );
    }

    private void hover(double x, double y) {
        hoverRow = getRowFromY(y);
        hoverCol = getColFromX(x);

        boolean last = hoverEnabled;
        hoverEnabled = isValidPosition(x, y, hoverRow, hoverCol);

        if (last != hoverEnabled)
            circuitController.notifyCanvas();

    }

    private void select(double x, double y) {

        selectedEnabled = false;

        selectedRow = getRowFromY(y);
        selectedCol = getColFromX(x);

        if (isValidPosition(x, y, selectedRow, selectedCol)) {
            addComponent(selectedRow, selectedCol);
            hover(x, y);
        } else {
            Integer r = getComponentRow(selectedRow, selectedCol);
            if (r != null) {
                selectedSpan = components[r][selectedCol].getSpan();
                selectedRow = r;
                selectedEnabled = true;
            }
        }

        removeComponentButton.setDisable(!selectedEnabled);
        controlButton.setDisable(!(canControl(selectedRow, selectedCol) && selectedEnabled));
        classicalControlButton.setDisable(!(selectedEnabled && !components[selectedRow][selectedCol].isGate()));

        if (classicControlMode && canClassicalControl(selectedRow, selectedCol)) {
            StandardGate gate = (StandardGate) components[selectedRow][selectedCol];
            QuantumMeter meter = (QuantumMeter) components[classicRow][classicCol];
            gate.setQuantumMeter(meter);
            circuitController.notifyIndicatorLabelChange("We good lads");
            circuitController.notifyIndicatorBarChange(Color.rgb(0, 200, 0, 0.5));
            circuitController.notifyState();
        } else if (classicControlMode) {
            showErrorDialog("Failed to apply classical control");
            classicControlMode = false;
            circuitController.notifyIndicatorLabelChange("We good lads");
            circuitController.notifyIndicatorBarChange(Color.rgb(0, 200, 0, 0.5));
        }

        circuitController.notifyCanvas();
    }

    private void control(int row, int col) {
        components[row - 1][col] = new ControlledGate((StandardGate) components[row][col]);
        components[row][col] = null;
        selectedRow--;
        selectedSpan++;
        controlButton.setDisable(!(canControl(selectedRow, selectedCol) && selectedEnabled));

        circuitController.notifyState();
    }

    private boolean canControl(int row, int col) {
        if (row <= 0) return false;
        if (containsComponent(row - 1, col)) return false;
        return true;
    }

    private boolean classicControlMode = false;
    private int classicRow, classicCol;
    private void initiateClassicControl(int row, int col) {
        circuitController.notifyIndicatorLabelChange("Select component to connect to measurement device");
        circuitController.notifyIndicatorBarChange(Color.ORANGE);
        classicControlMode = true;
        classicRow = row;
        classicCol = col;
    }

    private boolean canClassicalControl(int row, int col) {
        if (components[row][col] != null && components[row][col].isGate())
            return true;
        return false;
    }

    private void addComponent(int row, int col) {

        Component component = null;
        switch (gateSelect.getValue()) {
            case "H":
                component = new StandardGate("H", new H(row));
                break;
            case "X":
                component = new StandardGate("X", new X(row));
                break;
            case "Y":
                component = new StandardGate("Y", new Y(row));
                break;
            case "Z":
                component = new StandardGate("Z", new Z(row));
                break;
            case "CNOT":
                component = new CNOTGate(row);
                break;
            case "SWAP":
                component = new SWAPGate(row);
                break;
            case "Measure":
                component = new QuantumMeter(row, col);
                break;
            case "R":
                try {
                    double phi = RunRGateWizard();
                    component = new StandardGate("R", new R(row, phi));
                } catch (Exception e) {
                    showErrorDialog("Failed to add R gate");
                }
                break;
            case "S":
                component = new StandardGate("S", new S(row));
                break;
            case "T":
                component = new StandardGate("T", new T(row));
                break;
            case "QFT":
                try {
                    Pair<Integer, Boolean> result = RunQFTGateWizard();
                    if (result == null)
                        break;

                    if (!result.getValue())
                        component = new StandardGate("QFT", new QFT(row, result.getKey()));
                    else
                        component = new StandardGate("QFT\u2020", new QFTHA(row, result.getKey()));
                } catch (Exception e) {
                    showErrorDialog("Failed to add QFT gate");
                }
                break;
            case "Custom (Matrix)":
                try {
                    Pair<String, ComplexMatrix> result = RunCustomMatrixWizard();

                    if (result == null)
                        break;

                    Gate gate = new CustomGate(row, (int) (Math.log(result.getValue().rows) / Math.log(2)), result.getValue());
                    if (!gate.getMatrix().isUnitary()) {
                        showErrorDialog("Matrix is not unitary");
                    }

                    component = new StandardGate(result.getKey(), gate);

                } catch (Exception e) {
                    showErrorDialog("Failed to add custom gate");
                }
                break;
            default: break;
        }

        if (component != null) {

            // Can it be added?
            if (row + component.getSpan() > numWires) {
                showErrorDialog("Cannot place the selected component in this position.");
                return;
            } else if (doesOverlapComponent(component, row, col)) {
                showErrorDialog("Cannot place the selected component in this position.");
                return;
            }

            components[row][col] = component;
            circuitController.notifyState();
        }

    }

    private Pair<String, ComplexMatrix> RunCustomMatrixWizard() throws Exception {

        Dialog<Pair<String, ComplexMatrix>> dialog = new Dialog<>();
        dialog.setTitle("Define matrix");
        dialog.setHeaderText(null);
        dialog.setContentText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        GridPane content = new GridPane();
        content.setVgap(5);
        content.setHgap(10);

        List<String> gateSizes = new ArrayList<>();
        for (int i = 1; i <= numWires - selectedRow; i++) {
            gateSizes.add(String.valueOf(i));
        }

        ChoiceBox<String> gateSizeChoice = new ChoiceBox<>(FXCollections.observableArrayList(gateSizes));
        gateSizeChoice.setValue(gateSizes.get(0));

        GridPane entryPane = new GridPane();
        entryPane.setHgap(5);
        entryPane.setVgap(5);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                HBox complexBox = new HBox();
                TextField real = new TextField();
                real.setPrefWidth(70);
                real.setPromptText("Real");
                TextField im = new TextField();
                im.setPrefWidth(70);
                im.setPromptText("Imag");
                complexBox.getChildren().addAll(real, im);
                entryPane.add(complexBox, j, i);
            }
        }

        gateSizeChoice.getSelectionModel().selectedIndexProperty().addListener(((observable, oldValue, newValue) -> {
            if (!oldValue.equals(newValue)) {
                int dim = Integer.parseInt(gateSizeChoice.getItems().get(newValue.intValue())) << 1;
                entryPane.getChildren().clear();
                for (int i = 0; i < dim; i++) {
                    for (int j = 0; j < dim; j++) {
                        HBox complexBox = new HBox();
                        TextField real = new TextField();
                        real.setPrefWidth(70);
                        real.setPromptText("Real");
                        TextField im = new TextField();
                        im.setPrefWidth(70);
                        im.setPromptText("Imag");
                        complexBox.getChildren().addAll(real, im);
                        entryPane.add(complexBox, j, i);
                    }
                }
                dialog.getDialogPane().getScene().getWindow().sizeToScene();
            }
        }));

        content.add(new Label("Number of inputs"), 0, 0);
        content.add(gateSizeChoice, 1, 0);
        content.add(new Label("Gate label"), 0, 1);
        TextField label = new TextField();
        label.setMaxWidth(150);
        content.add(label, 1, 1);
        content.add(entryPane, 0, 2, 2, 1);

        dialog.getDialogPane().setContent(content);

        AtomicBoolean notifyError = new AtomicBoolean(false);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int size = Integer.parseInt(gateSizeChoice.getValue()) << 1;
                    ComplexMatrix complexMatrix = new ComplexMatrix(size, size);
                    for (int i = 0; i < size; i++) {
                        for (int j = 0; j < size; j++) {
                            HBox complexBox = (HBox) entryPane.getChildren().get(i * size + j);
                            TextField real = (TextField) complexBox.getChildren().get(0);
                            TextField imag = (TextField) complexBox.getChildren().get(1);
                            complexMatrix.data[i][j].real = Double.parseDouble(real.getText());
                            complexMatrix.data[i][j].imaginary = Double.parseDouble(imag.getText());
                        }
                    }
                    return new Pair<>(label.getText(), complexMatrix);
                } catch (Exception e) {
                    notifyError.set(true);
                    return null;
                }
            }
            return null;
        });

        Optional<Pair<String, ComplexMatrix>> result = dialog.showAndWait();

        if (notifyError.get())
            throw new Exception();

        return result.orElse(null);
    }

    private Pair<Integer, Boolean> RunQFTGateWizard() throws Exception {

        Dialog<Pair<Integer, Boolean>> dialog = new Dialog<>();
        dialog.setTitle("Add QFT Gate");
        dialog.setWidth(150);
        dialog.setHeaderText(null);
        dialog.setContentText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);

        GridPane content = new GridPane();
        content.setVgap(5);
        content.setHgap(10);

        List<String> gateSizes = new ArrayList<>();
        for (int i = 1; i <= numWires - selectedRow; i++) {
            gateSizes.add(String.valueOf(i));
        }
        ChoiceBox<String> gateSizeChoice = new ChoiceBox<>(FXCollections.observableArrayList(gateSizes));
        gateSizeChoice.setValue(gateSizes.get(0));

        CheckBox invertCheckbox = new CheckBox();

        content.add(new Label("Number of inputs"), 0, 0);
        content.add(gateSizeChoice, 1, 0);
        content.add(new Label("Invert"), 0, 1);
        content.add(invertCheckbox, 1, 1);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(Integer.valueOf(gateSizeChoice.getValue()), invertCheckbox.isSelected());
            }
            return null;
        });

        Optional<Pair<Integer, Boolean>> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private double RunRGateWizard() throws Exception {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setWidth(150);
        dialog.setTitle("Add R (Phase) Gate");
        dialog.setHeaderText(null);
        dialog.setContentText("Phase shift (radians):");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            return Double.parseDouble(result.get());
        } else {
            throw new Exception("No input");
        }
    }

    private void removeComponent(int row, int col) {
        if (components[row][col] != null && selectedEnabled) {
            components[row][col].cleanUp();
            components[row][col] = null;
            selectedEnabled = false;
            removeComponentButton.setDisable(true);
            controlButton.setDisable(true);

            circuitController.notifyState();
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Circuit Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.setWidth(150);
        alert.showAndWait();
    }

    private boolean isValidPosition(double x, double y, int row, int col) {

        if (col == numGates - 1)
            return false;

        boolean valid = !(x > getXFromCol(col) + gateWidth);
        if (valid)
            valid = !(x < getXFromCol(col));
        if (valid)
            valid = !(y > getYFromRow(row) + gateHeight);
        if (valid)
            valid = !(y < getYFromRow(row));
        if (valid) {
            valid = !containsComponent(row, col);
        }
        return valid;
    }

    private boolean doesOverlapComponent(Component component, int row, int col) {
        for (int i = 0; i < numWires; i++) {
            if (components[i][col] != null) {
                if (row >= i && row <= i + components[i][col].getSpan() - 1) {
                    return true;
                } else if (row + component.getSpan() - 1 >= i && row + component.getSpan() - 1 <= i + components[i][col].getSpan() - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsComponent(int row, int col) {
        for (int i = 0; i < numWires; i++) {
            if (components[i][col] != null) {
                if (row >= i && row <= i + components[i][col].getSpan() - 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private Integer getComponentRow(int row, int col) {
        for (int i = 0; i < numWires; i++) {
            if (components[i][col] != null) {
                if (row >= i && row <= i + components[i][col].getSpan() - 1) {
                    return i;
                }
            }
        }
        return null;
    }

    public static double getYFromRow(int row) {
        return (wireGap + gateHeight) * row + wireGap;
    }
    private double getXFromCol(int col) {
        return (gateGap + gateWidth) * col + gateGap;
    }
    private int getRowFromY(double y) {
        return (int) ((y - wireGap) / (wireGap + gateHeight));
    }
    private int getColFromX(double x) {
        return (int) ((x - gateGap) / (gateGap + gateWidth));
    }

    public void draw(GraphicsContext graphicsContext) {
        // Clear circuit
        graphicsContext.setFill(Color.WHITE);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw wires
        graphicsContext.setStroke(Color.BLACK);
        for (int i = 0; i < numWires; i++) {
            double y = getYFromRow(i) + gateHeight / 2.0;
            graphicsContext.strokeLine(0, y, canvas.getWidth(), y);
        }

        // Draw components
        for (int j = 0; j < numGates; j++) {
            for (int i = 0; i < numWires; i++) {
                if (components[i][j] != null) {
                    double x = getXFromCol(j);
                    double y = getYFromRow(i);
                    components[i][j].draw(x, y, graphicsContext);
                } else if (!containsComponent(i, j)) {
                    graphicsContext.setStroke(Color.rgb(0, 0, 0, 0.1));
                    graphicsContext.strokeRect(getXFromCol(j), getYFromRow(i), gateWidth, gateHeight);
                }
            }
        }

        // Draw hover
        if (hoverEnabled) {
            graphicsContext.setFill(Color.rgb(230, 230, 230, 0.8));
            graphicsContext.fillRect(getXFromCol(hoverCol), getYFromRow(hoverRow), gateWidth, gateHeight);
            graphicsContext.setStroke(Color.rgb(165, 137, 193, 1.0));
            graphicsContext.setLineWidth(2.5);
            graphicsContext.strokeLine(
                    getXFromCol(hoverCol) + gateWidth / 2.0 - 10, getYFromRow(hoverRow) + gateHeight / 2.0,
                    getXFromCol(hoverCol) + gateWidth / 2.0 + 10, getYFromRow(hoverRow) + gateHeight / 2.0
            );
            graphicsContext.strokeLine(
                    getXFromCol(hoverCol) + gateWidth / 2.0, getYFromRow(hoverRow) + gateHeight / 2.0 - 10,
                    getXFromCol(hoverCol) + gateWidth / 2.0, getYFromRow(hoverRow) + gateHeight / 2.0 + 10
            );
            graphicsContext.setLineWidth(1);
        }

        if (selectedEnabled) {
            graphicsContext.setFill(Color.rgb(165, 137, 193, 0.4));
            graphicsContext.fillRect(
                    getXFromCol(selectedCol), getYFromRow(selectedRow), gateWidth,
                    gateHeight + (selectedSpan - 1) * (gateHeight + wireGap)
            );
        }

//        // Draw selected overlay on component
//        if (selectedEnabled) {
//            if (!components[selectedRow][selectedCol].isGate()) {
//
//                graphicsContext.setFill(Color.rgb(230, 230, 230, 0.95));
//                graphicsContext.fillRect(
//                        getXFromCol(selectedCol), getYFromRow(selectedRow), gateWidth,
//                        gateHeight + (selectedSpan - 1) * (gateHeight + wireGap)
//                );
//
//                graphicsContext.setStroke(Color.rgb(0, 200, 0, 0.5));
//                graphicsContext.setLineWidth(3);
//                graphicsContext.strokeLine(
//                        getXFromCol(selectedCol) + gateWidth / 2.0 - 10, getYFromRow(selectedRow) + gateHeight / 2.0,
//                        getXFromCol(selectedCol) + gateWidth / 2.0 + 10, getYFromRow(selectedRow) + gateHeight / 2.0
//                );
//                graphicsContext.strokeLine(
//                        getXFromCol(selectedCol) + gateWidth / 2.0, getYFromRow(selectedRow) + gateHeight / 2.0 - 10,
//                        getXFromCol(selectedCol) + gateWidth / 2.0, getYFromRow(selectedRow) + gateHeight / 2.0 + 10
//                );
//                graphicsContext.setLineWidth(1);
//            } else {
//                graphicsContext.setFill(Color.rgb(165, 137, 193, 0.4));
//                graphicsContext.fillRect(
//                        getXFromCol(selectedCol), getYFromRow(selectedRow), gateWidth,
//                        gateHeight + (selectedSpan - 1) * (gateHeight + wireGap)
//                );
//            }
//        }

        drawMeasurements(graphicsContext);
    }

    private double[] measurements;

    public void setMeasurements(double[] measurements) {
        this.measurements = measurements;
    }

    private void drawMeasurements(GraphicsContext graphicsContext) {

        if (measurements == null) {
            measurements = new double[numWires];
            Arrays.fill(measurements, 0);
        } else if (measurements.length != numWires) {
            double[] newMeasurements = new double[numWires];
            for (int i = 0; i < Math.min(measurements.length, numWires); i++)
                newMeasurements[i] = measurements[i];
            measurements = newMeasurements;
        }


        for (int i = 0; i < numWires; i++) {

            graphicsContext.setFill(Color.WHITESMOKE);
            graphicsContext.fillRect(getXFromCol(numGates - 1), getYFromRow(i), gateWidth, gateHeight);

            graphicsContext.setFill(Color.rgb(0, 200, 0, 0.5));
            graphicsContext.fillRect(getXFromCol(numGates  - 1), getYFromRow(i) + gateHeight - gateHeight * measurements[i], gateWidth, gateHeight * measurements[i]);

            String percentage = String.format("%.1f", measurements[i] * 100.0);
            graphicsContext.setTextAlign(TextAlignment.CENTER);
            graphicsContext.setTextBaseline(VPos.CENTER);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.fillText(percentage + "%", getXFromCol(numGates - 1) + gateWidth / 2.0, getYFromRow(i) + gateHeight / 2.0, gateWidth - 10);

        }

    }

}
