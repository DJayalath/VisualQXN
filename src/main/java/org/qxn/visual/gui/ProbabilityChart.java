package org.qxn.visual.gui;

import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;


public class ProbabilityChart {

    public BarChart<String, Number> getBarChart() {
        return barChart;
    }

    private final BarChart<String, Number> barChart;

    public ProbabilityChart(ObservableValue
                                    <? extends Number> widthProperty) {

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setAnimated(false);
        xAxis.setLabel("Output");
        yAxis.setLabel("Probability");

        barChart.setLegendVisible(false);
        barChart.prefWidthProperty().bind(widthProperty);

    }

    public void updateBarChart(double[] p) {

        int numWires = p.length >> 1;
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (int i = 0; i < p.length; i++) {
            String binary = String.format("%" + numWires + "s",
                    Integer.toBinaryString(i)).replace(' ', '0');
            series.getData().add(new XYChart.Data<>(binary, p[i]));
        }

        barChart.getData().add(series);

    }

}
