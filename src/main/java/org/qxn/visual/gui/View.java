package org.qxn.visual.gui;

import javafx.application.Application;
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

        GridPane leftPane = new GridPane();
        GridPane centrePane = new GridPane();
        GridPane bottomPane = new GridPane();

        rootPane.setLeft(leftPane);
        rootPane.setCenter(centrePane);
        rootPane.setBottom(bottomPane);

        // Build circuit configuration
        bottomPane.setVgap(5.0);
        bottomPane.setHgap(5.0);
        bottomPane.setAlignment(Pos.CENTER);
        Label bottomTitle = new Label("Circuit Configuration");
        bottomTitle.setPadding(new Insets(10));

        // Left Button pane
        GridPane buttonPane = new GridPane();
        leftPane.add(buttonPane, 0, 0);

        // Left Circuit pane
        Canvas circuitDrawing = new Canvas();

        circuitDrawing.setWidth(600);
        circuitDrawing.setHeight(200);

        GraphicsContext gc = circuitDrawing.getGraphicsContext2D();

        CircuitController circuitController = new CircuitController(3, gc);
//        try {
//            circuitController.addGate(new VGate("H", 0, 0, 1));
//            circuitController.addGate(new VGate("CNOT", 1, 1, 2));
//            VGate m = new VGate("M", 1, 2, 1);
//            VGate hc = new VGate("H", 2, 3, 1);
//            hc.setConnected(m);
//            circuitController.addGate(m);
//            circuitController.addGate(hc);
//        } catch (CircuitPlacementException e) {
//            e.printStackTrace();
//        }

        circuitDrawing.setOnMouseClicked(event -> circuitController.select(event.getX(), event.getY()));

        circuitController.draw();

        GridPane circuitPane = new GridPane();
        circuitPane.setPadding(new Insets(30));
        circuitPane.add(circuitDrawing, 0, 0);
        leftPane.add(circuitPane, 1, 0);

        buttonPane.setPadding(new Insets(5));
        buttonPane.setHgap(5);
        buttonPane.setVgap(5);


        Button add = new Button("+");
        add.setMaxWidth(Double.MAX_VALUE);
        add.setOnMouseClicked(event -> {
            List<String> choices = new ArrayList<>();
            choices.add("H");
            choices.add("X");
            choices.add("Y");
            choices.add("Z");
            choices.add("SWAP");
            choices.add("CNOT");
            choices.add("Measure");

            ChoiceDialog<String> dialog = new ChoiceDialog<>("H", choices);
            dialog.setTitle("Add new gate");
            dialog.setHeaderText("Choose gate to add");
            dialog.setContentText("Choose gate:");

            // Traditional way to get the response value.
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()){
                try {
                    switch (result.get()) {
                        case "Z":
                        case "X":
                        case "Y":
                        case "H":
                            circuitController.addGate(new VGate(result.get(), circuitController.getSelectedX(), circuitController.getSelectedY(), 1));
                            break;
                        case "SWAP":
                        case "CNOT":
                            circuitController.addGate(new VGate(result.get(), circuitController.getSelectedX(), circuitController.getSelectedY(), 2));
                            break;
                        case "Measure":
                            circuitController.addGate(new VGate("M", circuitController.getSelectedX(), circuitController.getSelectedY(), 1));
                        default:
                            break;
                    }
                } catch (CircuitPlacementException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Circuit Placement Error");
                    alert.setHeaderText("Error placing gate");
                    alert.setContentText("Cannot place this gate in the specified position.");

                    alert.showAndWait();
                }
            }
        });


        Button remove = new Button("-");
        remove.setMaxWidth(Double.MAX_VALUE);
        remove.setOnMouseClicked(event -> circuitController.removeGate());


        centrePane.setPadding(new Insets(20));
        // Bar chart

//        centrePane.add(barChart, 0, 0);
        leftPane.add(circuitController.getBarChart(), 1, 1);

        Group group = new Group();

        Circle circle = new Circle();
        circle.setFill(Color.LIGHTGREEN);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2.0);
        circle.setRadius(60);

        Ellipse ellipseX = new Ellipse(60,10);
        ellipseX.setStroke(Color.DARKGRAY);
        ellipseX.setStrokeWidth(2.0);
        ellipseX.setFill(Color.TRANSPARENT);

        Ellipse ellipseY = new Ellipse(10,60);
        ellipseY.setStroke(Color.DARKGRAY);
        ellipseY.setStrokeWidth(2.0);
        ellipseY.setFill(Color.TRANSPARENT);

        Line line = new Line(8, 8, -8, -8);
        line.setStroke(Color.DARKGRAY);
        line.setFill(Color.DARKGRAY);

        // In a block sphere for state a|0> + b|1>
        // a = cos(theta / 2)
        // b = e^{i * phi} * sin(theta  / 2)

        // Therefore theta = 2 * arcos(a)
        ComplexMatrix complex = new ComplexMatrix(2, 1);
//        complex.data[0][0].real = 1.0 / Math.sqrt(2.0);
//        complex.data[1][0].real = 1.0 / Math.sqrt(2.0);
        complex.data[1][0].real = 1.0;


        // Choose A to be real
        double theta = 2 * Math.acos(complex.data[0][0].real);
        Complex eBit = complexDivide(complex.data[1][0], new Complex(Math.sin(theta / 2.0), 0));
        double phi = Math.asin(eBit.real);
        double xc = circle.getRadius() * Math.cos(-Math.PI / 2.0 + theta);
        double yc = circle.getRadius() * Math.sin(-Math.PI / 2.0 + theta);
        Circle point2 = new Circle(xc, yc, 5.0, Color.RED);

        // x = acos(p)
        // y = bsin(p)
        double x = ellipseX.getRadiusX() * Math.cos(Math.PI / 2);
        double y = ellipseX.getRadiusY() * Math.sin(Math.PI / 2);
        Circle point = new Circle(x, y, 5.0, Color.RED);

        group.getChildren().addAll(circle, ellipseX, ellipseY, line, point, point2);
//        sphere.setDrawMode(DrawMode.LINE);

//        centrePane.add(group, 0, 1);

        Button run = new Button("-->");
        run.setMaxWidth(Double.MAX_VALUE);
        run.setOnMouseClicked(event -> circuitController.execute());

        Button addWire = new Button("Q+");
        addWire.setMaxWidth(Double.MAX_VALUE);
        addWire.setOnMouseClicked(event -> circuitController.addWire());
        Button removeWire = new Button("Q-");
        removeWire.setMaxWidth(Double.MAX_VALUE);
        removeWire.setOnMouseClicked(event -> circuitController.removeWire());

        Button connect = new Button("<>");
        connect.setMaxWidth(Double.MAX_VALUE);
        connect.setOnMouseClicked(event -> circuitController.connect());

        Button breakPoint = new Button("BP");
        breakPoint.setMaxWidth(Double.MAX_VALUE);
        breakPoint.setOnMouseClicked(event -> {
            try {
                circuitController.addGate(new VGate("BP", circuitController.getSelectedX(), circuitController.getSelectedY(), circuitController.getNumWires()));
            } catch (CircuitPlacementException e) {
                e.printStackTrace();
            }
        });

        Button stepForward = new Button("STEP FWD");
        stepForward.setMaxWidth(Double.MAX_VALUE);
        stepForward.setOnMouseClicked(event -> circuitController.stepForward());

        Button stepBackward = new Button("STEP BWD");
        stepBackward.setMaxWidth(Double.MAX_VALUE);
        stepBackward.setOnMouseClicked(event -> circuitController.stepBackward());

        buttonPane.add(add, 0, 0);
        buttonPane.add(remove, 0, 1);
        buttonPane.add(run, 0, 2);
        buttonPane.add(addWire, 0, 3);
        buttonPane.add(removeWire, 0, 4);
        buttonPane.add(connect, 0, 5);
        buttonPane.add(breakPoint, 0, 6);
        buttonPane.add(stepForward, 0, 7);
        buttonPane.add(stepBackward, 0, 8);

        Scene scene = new Scene(rootPane);
        stage.setScene(scene);
//        stage.setMaximized(true);
        stage.setTitle("VisualQXN");

        stage.show();

    }

    Complex complexDivide(Complex a, Complex b) {
        Complex c = new Complex(0, 0);
        c.real = (a.real * b.real + a.imaginary * b.imaginary) / (Math.pow(b.real, 2) + Math.pow(b.imaginary, 2));
        c.imaginary = (a.imaginary * b.real - a.real * b.imaginary) / (Math.pow(b.real, 2) + Math.pow(b.imaginary, 2));
        return c;
    }
}
