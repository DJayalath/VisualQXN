package org.qxn.visual.gui;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import org.qxn.QuantumMachine;
import org.qxn.gates.H;
import org.qxn.gates.X;
import org.qxn.gates.Z;

import java.io.*;
import java.util.*;

// Preserves (serializable) state of this class
class State implements Serializable {
    public int numWires;
    public int selectedRow;
    public int selectedCol;
    public double selectedHeight;
    public LinkedList<Integer> breakPoints;
    public Component[][] components;
}

public class Circuit {

    public static int maxWires = 10;
    public static final double boxWidth = 50;
    public static final double boxHeight = 50;
    public static final int rowDist = 20;
    public static final int colDist = 20;
    public static int maxGates = 15;
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private final BarChart<String, Number> barChart;
    private final Label notification = new Label();
    private final Button addWireButton = new Button("Wire +");
    private final Button removeWireButton = new Button("Wire -");
    private final Button controlButton = new Button("Control");
    private final Circle indicator = new Circle(5, Color.ORANGE);
    private final List<double[]> probabilities = new ArrayList<>();
    private LinkedList<Integer> breakPoints = new LinkedList<>();
    private final Button stepForward = new Button("Step Forward");
    private final Button stepBackward = new Button("Step Backward");
    private final List<double[]> onIfMeasured = new ArrayList<>();
    private final Stack<State> undoStack = new Stack<>();
    private final Stack<State> redoStack = new Stack<>();

    public Button getUndoButton() {
        return undoButton;
    }

    public Button getRedoButton() {
        return redoButton;
    }

    private final Button undoButton = new Button("Undo");
    private final Button redoButton = new Button("Redo");
    List<double[]> queuedMeasure = new ArrayList<>();
    private Component[][] components;
    private int numWires;
    private int selectedRow;
    private int selectedCol;
    private double selectedHeight;
    private int step = 0;
    private QMeter connectingComponent;
    private int connecting = 0;
    private int hoverCol, hoverRow;
    private double hoverHeight;
    private boolean hoverDisable = true;

    public void save(String path) throws IOException {
        State state = getState();

        FileOutputStream file = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(file);

        out.writeObject(state);

        out.close();
        file.close();
    }

    public void load(String path) throws IOException, ClassNotFoundException {

        FileInputStream file = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(file);

        State state = (State) in.readObject();

        in.close();
        file.close();

        restoreState(state);
        resetRun();
        breakPoints.clear();

    }

    private State getState() {
        State state = new State();
        state.numWires = numWires;
        state.selectedRow = selectedRow;
        state.selectedCol = selectedCol;
        state.selectedHeight = selectedHeight;

        state.breakPoints = new LinkedList<>(breakPoints);

        state.components = new Component[maxWires][maxGates];
        for (int i = 0; i < numWires; i++)
            for (int j = 0; j < maxGates; j++)
                state.components[i][j] = components[i][j];

        return state;
    }

    private void addUndoState() {
        undoStack.push(getState());
        if (undoButton.isDisable())
            undoButton.setDisable(false);
    }

    private void addRedoState() {
        redoStack.push(getState());
        if (redoButton.isDisable())
            redoButton.setDisable(false);
    }

    private void undo() {
        addRedoState();
        restoreState(undoStack.pop());
        if (undoStack.isEmpty())
            undoButton.setDisable(true);
    }

    private void redo() {
        addUndoState();
        restoreState(redoStack.pop());
        if (redoStack.isEmpty())
            redoButton.setDisable(true);
    }

    public void restoreState(State state) {

        while (state.numWires > numWires)
            addWire();

        while (state.numWires < numWires)
            removeWire();

        if (state.components[0].length != components[0].length) {
            maxGates = state.components[0].length;
            canvas.setWidth(colDist + boxWidth * maxGates + colDist * maxGates);
            barChart.setPrefWidth(canvas.getWidth());

            if (selectedCol >= maxGates)
                selectedCol = maxGates - 1;

            expandSelection();
            resetHistory();
        }

        selectedRow = state.selectedRow;
        selectedCol = state.selectedCol;
        selectedHeight = state.selectedHeight;
        breakPoints = state.breakPoints;
        components = state.components;

        resetRun();
        draw();
    }

    public void resetHistory() {
        undoStack.clear();
        redoStack.clear();
        undoButton.setDisable(true);
        redoButton.setDisable(true);
    }

    public Circuit(int numWires, int maxGates) {
        Circuit.maxGates = maxGates;
        this.canvas = new Canvas(colDist + boxWidth * maxGates + colDist * maxGates, rowDist + boxHeight * numWires + rowDist * numWires);
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.numWires = numWires;
        this.components = new Component[maxWires][maxGates];

        canvas.setOnMouseMoved(e -> hover(e.getX(), e.getY()));
        canvas.setOnMouseExited(e -> {hoverDisable = true; draw();});
        controlButton.setDisable(true);
        controlButton.setOnMouseClicked(e -> control());

        undoButton.setOnMouseClicked(e -> undo());
        undoButton.setDisable(true);
        redoButton.setOnMouseClicked(e -> redo());
        redoButton.setDisable(true);

        stepForward.setOnMouseClicked(event -> next());
        stepBackward.setOnMouseClicked(event -> previous());
        stepForward.setDisable(true);
        stepBackward.setDisable(true);

        addWireButton.setOnMouseClicked(event -> addWire());
        removeWireButton.setOnMouseClicked(event -> removeWire());

        // Initialise bar chart

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        xAxis.setLabel("Output");
        yAxis.setLabel("Probability");

        barChart.setLegendVisible(false);
        barChart.setPrefWidth(canvas.getWidth());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 1 << numWires; i++) {
            String binary = String.format("%" + numWires + "s",
                    Integer.toBinaryString(i)).replace(' ', '0');
            series.getData().add(new XYChart.Data<>(binary, (i == 0) ? 1.0 : 0.0));
        }
        barChart.getData().add(series);

        this.components[0][0] = new StandardGate(0, 0, 1, new H(0));
        this.components[0][1] = new CNOTGate(0, 1);
        this.components[1][2] = new SWAPGate(1, 2);
        this.components[1][3] = new QMeter(1, 3);
        this.components[3][4] = new StandardGate(3, 4, 1, new Z(0));
        StandardGate xConnected = new StandardGate(2, 5, 1, new X(2));
        xConnected.setqMeter((QMeter) this.components[1][3]);
        ((QMeter) this.components[1][3]).setConnected(xConnected);
        this.components[2][5] = xConnected;

        select(0, 0);
        hover(0, 0);
    }

    public static double getXFromCol(double col) {
        return colDist + col * (boxWidth + colDist);
    }

    public static double getYFromRow(double row) {
        return rowDist + row * (boxHeight + rowDist);
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedCol() {
        return selectedCol;
    }

    public void resize(int newMaxGates) {

        Component[][] newComponents = new Component[maxWires][newMaxGates];
        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < Math.min(maxGates, newMaxGates); j++) {
                newComponents[i][j] = components[i][j];
            }
        }

        maxGates = newMaxGates;
        canvas.setWidth(colDist + boxWidth * maxGates + colDist * maxGates);

        components = newComponents;
        barChart.setPrefWidth(canvas.getWidth());

        if (selectedCol >= maxGates)
            selectedCol = maxGates - 1;

        expandSelection();
        resetHistory();
        resetRun();
    }

    public Button getStepForward() {
        return stepForward;
    }

    public Button getStepBackward() {
        return stepBackward;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public BarChart<String, Number> getBarChart() {
        return barChart;
    }

    public Circle getIndicator() {
        return indicator;
    }

    public void addComponent(Component component) throws CircuitException {

        addUndoState();

        // Check if number of wires supports component
        if (selectedRow + component.getSpan() > numWires)
            throw new CircuitException("Cannot place component here");

        // Check if space already occupied
        for (int i = 0; i < numWires; i++)
            if (components[i][selectedCol] != null) {

                double newStart = selectedRow;
                double newEnd = selectedRow + component.getSpan() - 1;
                double currentStart = i;
                double currentEnd = i + components[i][selectedCol].getSpan() - 1;

                if (newStart >= currentStart && newStart <= currentEnd) {
                    // Reject
                    throw new CircuitException("Cannot place component here");
                } else if (newEnd >= currentStart && newEnd <= currentEnd) {
                    // Reject
                    throw new CircuitException("Cannot place component here");
                }
            }

        resetRun();
        components[selectedRow][selectedCol] = component;
        expandSelection();

        draw();
    }

    public void removeComponent() {

        addUndoState();

        if (components[selectedRow][selectedCol] != null) {
            if (components[selectedRow][selectedCol] instanceof QMeter) {
                if (((QMeter) components[selectedRow][selectedCol]).getConnected() != null)
                    ((QMeter) components[selectedRow][selectedCol]).getConnected().setqMeter(null);
            }
            resetRun();
            components[selectedRow][selectedCol] = null;
        }

        expandSelection();

        draw();

    }

    private void expandHover() {
        // Expand selection to occupy component
        hoverHeight = boxHeight;
        for (int i = 0; i < numWires; i++)
            if (components[i][hoverCol] != null) {
                if (hoverRow >= i && hoverRow < i + components[i][hoverCol].getSpan()) {
                    hoverRow = i;
                    hoverHeight = boxHeight + (boxHeight + rowDist) * (components[i][hoverCol].getSpan() - 1);
                }
            }
    }

    private void expandSelection() {
        // Expand selection to occupy component
        selectedHeight = boxHeight;
        for (int i = 0; i < numWires; i++)
            if (components[i][selectedCol] != null) {
                if (selectedRow >= i && selectedRow < i + components[i][selectedCol].getSpan()) {
                    selectedRow = i;
                    selectedHeight = boxHeight + (boxHeight + rowDist) * (components[i][selectedCol].getSpan() - 1);
                }
            }

        // Can control?
        if (selectedRow > 0 && components[selectedRow - 1][selectedCol] == null) {
            controlButton.setDisable(!(components[selectedRow][selectedCol] instanceof StandardGate));
        } else controlButton.setDisable(true);
    }

    public void control() {

        addUndoState();

        components[selectedRow - 1][selectedCol] = new ControlledGate((StandardGate) components[selectedRow][selectedCol]);
        components[selectedRow][selectedCol] = null;

        selectedRow -= 1;
        expandSelection();
        draw();

    }

    public void hover(double x, double y) {
        // Find selected box
        x -= colDist;
        y -= rowDist;

        // DO NOT ALLOW SELECTION OF LAST POSITION (reserved for percentage bar)
        if ((int) (x / (boxWidth + colDist)) < maxGates - 1) {
            hoverCol = (int) (x / (boxWidth + colDist));
            hoverRow = (int) (y / (boxHeight + rowDist));
        }

        hoverDisable = x > hoverCol * (boxWidth + colDist) + boxWidth || y > hoverRow * (boxHeight + colDist) + boxHeight;

        if (components[hoverRow][hoverCol] != null && x <= hoverCol * (boxWidth + colDist) + boxWidth)
            hoverDisable = false;

        expandHover();
        highlightBreakPosition(x);

        draw();
    }

    double breakHighlight;
    public void highlightBreakPosition(double x) {
        if (x > hoverCol * (boxWidth + colDist) + boxWidth)
            breakHighlight = colDist + hoverCol * (boxWidth + colDist) + boxWidth + colDist / 2.0;
    }

    public Button getControlButton() {
        return controlButton;
    }

    public void clear() {
        addUndoState();
        for (int i = 0; i < numWires; i++) {
            for (int j = 0; j < maxGates; j++) {
                components[i][j] = null;
            }
        }
        expandSelection();
        resetRun();
    }

    public void select(double x, double y) {

        // Find selected box
        x -= colDist;
        y -= rowDist;

        // Toggle breakpoint
        if (x > hoverCol * (boxWidth + colDist) + boxWidth)
            toggleBreakPoint((int) (x / (boxWidth + colDist)));
        else {
            // DO NOT ALLOW SELECTION OF LAST POSITION (reserved for percentage bar)
            if ((int) (x / (boxWidth + colDist)) < maxGates - 1) {
                selectedCol = (int) (x / (boxWidth + colDist));
                selectedRow = (int) (y / (boxHeight + rowDist));
            }
        }

        expandSelection();

        if (connecting == 1) {
            if (components[selectedRow][selectedCol] != null) {
                if (components[selectedRow][selectedCol] instanceof QMeter) {
                    connectingComponent = (QMeter) components[selectedRow][selectedCol];
                    connecting++;
                    notification.setText("SELECT GATE");
                } else {
                    notification.setText("");
                    connecting = 0;
                }
            } else {
                notification.setText("");
                connecting = 0;
            }
        } else if (connecting == 2) {
            if (components[selectedRow][selectedCol] != null) {
                if (components[selectedRow][selectedCol] instanceof StandardGate) {
                    if (selectedRow > connectingComponent.getRow() && selectedCol > connectingComponent.getCol()) {
                        ((StandardGate) components[selectedRow][selectedCol]).setqMeter(connectingComponent);
                        connectingComponent.setConnected((StandardGate) components[selectedRow][selectedCol]);
                    }
                }
            }
            notification.setText("");
            connecting = 0;
        }

        draw();
    }

    public void draw() {
        // Clear canvas
        graphicsContext.setFill(Color.LIGHTGRAY);
        graphicsContext.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw border
//        graphicsContext.setStroke(Color.BLACK);
//        graphicsContext.setLineWidth(5);
//        graphicsContext.strokeRect(-10, 0, canvas.getWidth() + 20, canvas.getHeight());
//        graphicsContext.setLineWidth(1);

        // Draw wires
        graphicsContext.setStroke(Color.BLACK);
        for (int i = 0; i < numWires; i++) {
            double y = getYFromRow(i) + boxHeight / 2.0;
            graphicsContext.strokeLine(0, y, colDist + boxWidth * (colDist + maxGates), y);
        }

        // Draw components
        for (int i = 0; i < maxGates; i++)
            for (int j = numWires - 1; j >= 0; j--)
                if (components[j][i] != null)
                    components[j][i].draw(graphicsContext);

        // Draw hover
        if (!hoverDisable) {
            graphicsContext.setLineWidth(3);
            graphicsContext.setStroke(Color.GRAY);
            graphicsContext.strokeRect(colDist + hoverCol * (boxWidth + colDist), rowDist + hoverRow * (boxWidth + rowDist), boxWidth, hoverHeight);
            graphicsContext.setLineWidth(1);
        } else {
            // Draw breakpoint hover
            graphicsContext.setLineWidth(3);
            graphicsContext.setStroke(Color.DARKGRAY);
            graphicsContext.strokeLine(breakHighlight, 0, breakHighlight, canvas.getHeight());
            graphicsContext.setLineWidth(1);
        }

        // Draw selection
        graphicsContext.setLineWidth(3);
        graphicsContext.setStroke(Color.RED);
        graphicsContext.strokeRect(colDist + selectedCol * (boxWidth + colDist), rowDist + selectedRow * (boxWidth + rowDist), boxWidth, selectedHeight);
        graphicsContext.setLineWidth(1);

        // Draw break points/line
        graphicsContext.setLineWidth(5);
        for (int i : breakPoints) {
            if (i != maxGates - 1) {
                if (step >= 0 && i == breakPoints.get(step) && indicator.getFill() == Color.GREEN)
                    graphicsContext.setStroke(Color.LIGHTGREEN);
                else graphicsContext.setStroke(Color.RED);
                graphicsContext.strokeLine(getXFromCol(i) + boxWidth + colDist / 2.0, 0, getXFromCol(i) + boxWidth + colDist / 2.0, canvas.getHeight());
            }
        }
        graphicsContext.setLineWidth(1);

        // Draw IF measured probabilities
        double[] measures;
        if (!queuedMeasure.isEmpty()) {
            measures = queuedMeasure.get(0);
        } else {
            measures = new double[numWires];
            Arrays.fill(measures, 0.0);
        }
        for (int i = 0; i < numWires; i++) {
            graphicsContext.setFill(Color.WHITESMOKE);
            graphicsContext.fillRect(getXFromCol(maxGates - 1), getYFromRow(i), boxWidth, boxHeight);
            graphicsContext.setFill(new Color(0.0, 1.0, 0.0, 1.0));
            graphicsContext.fillRect(getXFromCol(maxGates - 1), getYFromRow(i) + boxHeight - boxHeight * measures[i], boxWidth, boxHeight * measures[i]);
            String percentage = String.format("%.1f", measures[i] * 100.0);
            graphicsContext.setTextAlign(TextAlignment.CENTER);
            graphicsContext.setTextBaseline(VPos.CENTER);
            graphicsContext.setFill(Color.BLACK);
            graphicsContext.fillText(percentage + "%", getXFromCol(maxGates - 1) + boxWidth / 2.0, getYFromRow(i) + boxHeight / 2.0);
        }
    }

    public Label getNotification() {
        return notification;
    }

    public void connect() {

        if (connecting == 0) {
            connecting = 1;
            notification.setTextFill(Color.RED);
            notification.setText("SELECT MEASUREMENT COMPONENT");
        }

    }

    public void addWire() {
        addUndoState();

        resetRun();
        numWires++;
        canvas.setHeight(rowDist + boxHeight * numWires + rowDist * numWires);

        if (numWires == maxWires)
            addWireButton.setDisable(true);

        removeWireButton.setDisable(false);

        draw();
    }

    public Button getAddWireButton() {
        return addWireButton;
    }

    public Button getRemoveWireButton() {
        return removeWireButton;
    }

    public void removeWire() {

        addUndoState();

        resetRun();
        numWires--;
        canvas.setHeight(rowDist + boxHeight * numWires + rowDist * numWires);

        // Remove gates
        for (int i = 0; i < numWires + 1; i++) {
            for (int j = 0; j < maxGates; j++) {
                if (components[i][j] != null) {
                    if (i + components[i][j].getSpan() > numWires)
                        components[i][j] = null;
                }
            }
        }

        if (numWires == 1)
            removeWireButton.setDisable(true);

        addWireButton.setDisable(false);

        if (selectedRow >= numWires)
            selectedRow = numWires - 1;
        expandSelection();

        draw();
    }

    public void run() {

        probabilities.clear();
        onIfMeasured.clear();
        step = -1;

        QuantumMachine quantumMachine = new QuantumMachine(numWires);

        for (int j = 0; j < maxGates; j++) {
            for (int i = 0; i < numWires; i++) {
                Component component = components[i][j];

                if (component != null) {

                    if (component instanceof StandardGate) {
                        if (((StandardGate) component).getqMeter() != null) {
                            if (((StandardGate) component).getqMeter().getClassicalBit() == 1)
                                quantumMachine.addGate(((StandardGate) component).getGate());
                        } else {
                            quantumMachine.addGate(((StandardGate) component).getGate());
                        }
                    } else if (component instanceof QMeter) {
                        ((QMeter) component).setClassicalBit(quantumMachine.measure(i));
                    }
                }
            }

            quantumMachine.execute();
            // Add results for this column
            double[] results = new double[1 << numWires];
            double[] measures = new double[numWires];
            for (int k = 0; k < 1 << numWires; k++) {
                results[k] = quantumMachine.getQubits().data[k][0].getMagnitude2();
                double p = results[k];
                for (int l = 0; l < numWires; l++) {
                    if ((k & (1 << (numWires - l - 1))) != 0) {
                        measures[l] += p;
                    }
                }
            }
            onIfMeasured.add(measures);
            probabilities.add(results);
        }

        indicator.setFill(Color.GREEN);

        next();
    }

    public void resetRun() {
        probabilities.clear();
        onIfMeasured.clear();
        queuedMeasure.clear();
        step = -1;
        indicator.setFill(Color.ORANGE);
        draw();
        stepForward.setDisable(true);
        stepBackward.setDisable(true);
    }

    public void toggleBreakPoint(int col) {
        if (!breakPoints.contains(col)) {
            breakPoints.add(col);
        } else {
            breakPoints.remove((Object) col);
        }
        breakPoints.sort(Integer::compareTo);
        resetRun();
        draw();
    }

    public void next() {

        step++;
        if (breakPoints.isEmpty()) {
            breakPoints.add(maxGates - 1);
            updateBarChart();
            breakPoints.clear();

            stepForward.setDisable(true);
            stepBackward.setDisable(true);
        } else {
            if (!breakPoints.contains(maxGates - 1))
                breakPoints.addLast(maxGates - 1);

            updateBarChart();

            stepForward.setDisable(step + 1 >= breakPoints.size());

            if (step > 0)
                stepBackward.setDisable(false);

        }

        draw();

    }

    public void previous() {

        step--;

        if (step > 0) {
            updateBarChart();
            draw();
            stepForward.setDisable(false);
        } else {
            step = 0;
            updateBarChart();
            draw();
            stepForward.setDisable(false);
            stepBackward.setDisable(true);
        }

    }

    private void updateBarChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        double[] results = probabilities.get(breakPoints.get(step));
        for (int i = 0; i < 1 << numWires; i++) {
            String binary = String.format("%" + numWires + "s",
                    Integer.toBinaryString(i)).replace(' ', '0');
            series.getData().add(new XYChart.Data<>(binary, results[i]));
        }

        barChart.getData().clear();
        barChart.getData().add(series);

        queuedMeasure.clear();
        queuedMeasure.add(onIfMeasured.get(breakPoints.get(step)));

    }

}
