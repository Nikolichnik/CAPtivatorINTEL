package guiElements;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class DoughnutChart extends PieChart {

    private final Circle innerCircle;

    public DoughnutChart(ObservableList<Data> pieData) {
        super(pieData);

        this.setLabelsVisible(false);
        this.setMaxSize(90, 90);
        
        Pane stack = new Pane();
        
        Text text = new Text("bla");  //pieData.get(0).toString()
        innerCircle = new Circle();
        
        innerCircle.setFill(Color.TRANSPARENT);  // do in CSS!
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(3);
        
        text.setBoundsType(TextBoundsType.VISUAL);
        
        stack.setMaxSize(90, 90);
        
        stack.getChildren().addAll(innerCircle, text);
        
        this.layoutChartChildren(0, 0, 90, 90);
    }

    @Override
    protected void layoutChartChildren(double top, double left, double contentWidth, double contentHeight) {
        super.layoutChartChildren(top, left, contentWidth, contentHeight);
        addInnerCircleIfNotPresent();
        updateInnerCircleLayout();
    }

    private void addInnerCircleIfNotPresent() {
        if (getData().size() > 0) {
            Node pie = getData().get(0).getNode();
            if (pie.getParent() instanceof Pane) {
                Pane parent = (Pane) pie.getParent();
                if (!parent.getChildren().contains(innerCircle)) {
                    parent.getChildren().add(innerCircle);
                }
            }
        }
    }

    private void updateInnerCircleLayout() {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;
        for (PieChart.Data data : getData()) {
            Node node = data.getNode();

            Bounds bounds = node.getBoundsInParent();
            if (bounds.getMinX() < minX) {
                minX = bounds.getMinX();
            }
            if (bounds.getMinY() < minY) {
                minY = bounds.getMinY();
            }
            if (bounds.getMaxX() > maxX) {
                maxX = bounds.getMaxX();
            }
            if (bounds.getMaxY() > maxY) {
                maxY = bounds.getMaxY();
            }
        }

        innerCircle.setCenterX(minX + (maxX - minX) / 2);
        innerCircle.setCenterY(minY + (maxY - minY) / 2);

        innerCircle.setRadius((maxX - minX) / 4);
    }
}
