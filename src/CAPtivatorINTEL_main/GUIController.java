package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import FileHandling.FileWriter;
import com.fazecast.jSerialComm.SerialPort;
import comms.CommHandler;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;

public class GUIController implements Initializable {

    private FileReader fileReader = new FileReader();
    private FileWriter fileWriter = new FileWriter();
    private CommHandler comms = new CommHandler();
    private SerialPort chosenPort;

    @FXML
    private Region leftPanel;

    @FXML
    private LineChart<?, ?> graph;

    @FXML
    private ComboBox<String> selectFileDrop;

    @FXML
    private ToggleButton readFileButton = new ToggleButton();

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
    private ToggleButton connectButton = new ToggleButton();

    @FXML
    void handleButtonAction(ActionEvent event) {

    }

    public void handleConnectClick() {
        if (connectButton.getText().equalsIgnoreCase("Connect")) {
            comms.setChosenPort(SerialPort.getCommPort(selectPortDrop.getValue()));
            chosenPort = comms.getChosenPort();
            if (chosenPort.openPort()) {
                connectButton.setText("Connected");
                selectPortDrop.setDisable(true);
            }
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll(dropItems);
    }

}
