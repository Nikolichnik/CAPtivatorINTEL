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
    private ComboBox<String> selectPortDrop;
    ObservableList<String> dropItems = comms.getPortList();

    @FXML
    private ToggleButton connectButton;   

    @FXML
    void handleButtonAction(ActionEvent event) {

    }

    public void handleConnectClick() {

    }

    public void handleFileReadClick() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println(dropItems);
        selectPortDrop = new ComboBox(dropItems);
        connectButton.setText("Ša je???");
    }

}
