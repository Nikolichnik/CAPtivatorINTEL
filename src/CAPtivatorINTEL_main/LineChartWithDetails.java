package CAPtivatorINTEL_main;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

/**
 * @author Nikolichnik
 */
public class LineChartWithDetails extends StackPane implements Initializable {

//    private StackPane base;
    
    private AnchorPane detailsWindow;

    private NumberAxis xAxis;
    private NumberAxis yAxis;

    private LineChart<?, ?> baseChart;

    public LineChartWithDetails() {
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        baseChart = new LineChart(xAxis, yAxis);
        
        getChildren().add(baseChart);
        
    }

    public LineChart getChart() {
        return baseChart;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        getChildren().add(baseChart);
        this.setStyle("-fx-background-color:  #F5F5F5;");
    }

}
