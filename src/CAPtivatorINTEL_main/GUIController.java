package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import FileHandling.FileWriter;
import comms.CommHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class GUIController implements Initializable {

    private CommHandler comms = new CommHandler();
    private FileReader fileReader = new FileReader();
    private FileWriter fileWriter = new FileWriter();

    @FXML
    private Region leftPanel;

    @FXML
    private LineChart<?, ?> graph;

    @FXML
    private ComboBox<String> selectFileDrop;

    @FXML
    private ToggleButton readFileButton;

    @FXML
    private Region bottomPanel;

    @FXML
    private TextField fileNameTextBox;

    @FXML
    private CheckBox writeFileCheck;

    @FXML
    private ComboBox<String> selectPortDrop = new ComboBox();
    ObservableList<String> dropItems = comms.getPortList();

    @FXML
    private ToggleButton connectButton;

    public void setData() {
        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll("COM1", "COM2", "COM3");
        System.out.println(dropItems);
    }

    @FXML
    void handleButtonAction(ActionEvent event) {

    }

    public void handleConnectClick() {
        if (connectButton.getText().equalsIgnoreCase("Connect")) {
            connectButton.setText("Connected");
        } else {
            connectButton.setText("Connect");
        }
    }

    public void handleFileReadClick() {
        if (readFileButton.getText().equalsIgnoreCase("Read file")) {
            readFileButton.setText("Reading file...");
        } else {
            readFileButton.setText("Read file");
        }
    }

    public void handlePortDropClick() {

    }

    public void test() {
        readFileButton.setText("mrš");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        assert dropItems != null : "fx:id=\"dropItems\" was not injected: check your FXML file 'FXMLDocument.fxml'.";
//        System.out.println(dropItems);
    }

}
