package CAPtivatorINTEL_main;

import java.text.DecimalFormat;
import java.util.List;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * @author Nikolichnik
 */
public class LineChartWithDetails extends StackPane {

//    private StackPane base;
    private AnchorPane detailsWindow;

    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private LineChart<?, ?> baseChart;

    public LineChartWithDetails() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        baseChart = new LineChart(xAxis, yAxis);
        baseChart.setCreateSymbols(false);
        baseChart.setAnimated(false);
        baseChart.setLegendSide(Side.BOTTOM);
        baseChart.setStyle("-fx-background-color: transparent");

        detailsWindow = new AnchorPane();
        detailsWindow.toFront();

        bindMouseEvents(baseChart, 0.3);

        rebuildChart();
    }

    public LineChart getChart() {
        return baseChart;
    }

    private class DetailsPopup extends VBox {

        private DetailsPopup() {
            setStyle("-fx-border-width: 1px; -fx-padding: 5 5 5 5px; -fx-border-color: gray; -fx-background-color: rgba(245,245,245,0.7);");
            setVisible(false);
        }

        public void showChartDescrpition(MouseEvent event) {
            getChildren().clear();

            Double xValue = (Double) baseChart.getXAxis().getValueForDisplay(event.getX());
            Double yValue = (Double) baseChart.getYAxis().getValueForDisplay(event.getY());

            HBox baseChartPopupRow = buildPopupRow(xValue, yValue);
            if (baseChartPopupRow != null) {
                getChildren().add(baseChartPopupRow);
            }
        }

        private HBox buildPopupRow(Double xValue, Double yValue) {
            DecimalFormat numFormat = new DecimalFormat("#.0");
            HBox popupRow = new HBox(10,new Label("[" + numFormat.format(yValue) + ", " + numFormat.format(xValue) + "]"));
            return popupRow;
        }     
    }

    private void bindMouseEvents(LineChart baseChart, Double strokeWidth) {
        final DetailsPopup detailsPopup = new DetailsPopup();
        getChildren().add(detailsWindow);
        detailsWindow.getChildren().add(detailsPopup);
        detailsWindow.prefHeightProperty().bind(heightProperty());
        detailsWindow.prefWidthProperty().bind(widthProperty());
        detailsWindow.setMouseTransparent(true);

        setOnMouseMoved(null);
        setMouseTransparent(false);

        final Axis xAxis = baseChart.getXAxis();
        final Axis yAxis = baseChart.getYAxis();

        final Line xLine = new Line();
        final Line yLine = new Line();
        yLine.setFill(Color.GRAY);
        xLine.setFill(Color.GRAY);
        yLine.setStrokeWidth(strokeWidth / 2);
        xLine.setStrokeWidth(strokeWidth / 2);
        xLine.setVisible(false);
        yLine.setVisible(false);

        final Node chartBackground = baseChart.lookup(".chart-plot-background");
        for (Node n : chartBackground.getParent().getChildrenUnmodifiable()) {
            if (n != chartBackground && n != xAxis && n != yAxis) {
                n.setMouseTransparent(true);
            }
        }
        chartBackground.setCursor(Cursor.CROSSHAIR);
        chartBackground.setOnMouseEntered((event) -> {
            chartBackground.getOnMouseMoved().handle(event);
            detailsPopup.setVisible(true);
            xLine.setVisible(true);
            yLine.setVisible(true);
            detailsWindow.getChildren().addAll(xLine, yLine);
        });
        chartBackground.setOnMouseExited((event) -> {
            detailsPopup.setVisible(false);
            xLine.setVisible(false);
            yLine.setVisible(false);
            detailsWindow.getChildren().removeAll(xLine, yLine);
        });
        chartBackground.setOnMouseMoved(event -> {
            double x = event.getX() + chartBackground.getLayoutX();
            double y = event.getY() + chartBackground.getLayoutY();

            xLine.setStartX(10);
            xLine.setEndX(detailsWindow.getWidth() - 10);
            xLine.setStartY(y + 5);
            xLine.setEndY(y + 5);

            yLine.setStartX(x + 5);
            yLine.setEndX(x + 5);
            yLine.setStartY(10);
            yLine.setEndY(detailsWindow.getHeight() - 10);

            detailsPopup.showChartDescrpition(event);

            if (y + detailsPopup.getHeight() + 10 < getHeight()) {
                AnchorPane.setTopAnchor(detailsPopup, y + 10);
            } else {
                AnchorPane.setTopAnchor(detailsPopup, y - 10 - detailsPopup.getHeight());
            }

            if (x + detailsPopup.getWidth() + 10 < getWidth()) {
                AnchorPane.setLeftAnchor(detailsPopup, x + 10);
            } else {
                AnchorPane.setLeftAnchor(detailsPopup, x - 10 - detailsPopup.getWidth());
            }
        });
    }

    private void rebuildChart() {
        getChildren().clear();
        getChildren().add(baseChart);
        getChildren().add(detailsWindow);
    }

}
