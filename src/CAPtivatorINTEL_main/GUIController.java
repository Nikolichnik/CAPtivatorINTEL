package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import FileHandling.FileWriter;
import com.fazecast.jSerialComm.SerialPort;
import comms.CommHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
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

    private double voltage = 0;
    private double current = 0;
    private double seconds = 0;

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
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
            if (chosenPort.openPort()) {
                connectButton.setText("Connected");
                selectPortDrop.setDisable(true);
            }

            Thread thread = new Thread() {
                @Override
                public void run() {

                    graph.setCreateSymbols(false);

                    XYChart.Series voltageData = new XYChart.Series();
                    voltageData.setName("Voltage");

                    XYChart.Series currentData = new XYChart.Series();
                    currentData.setName("Current");

                    graph.getData().addAll(voltageData, currentData);

                    Scanner scanner = new Scanner(chosenPort.getInputStream());

                    while (scanner.hasNextLine()) {

                        try {
                            String line = scanner.nextLine();
                            System.out.println(line);
                            List<Double> linijaPodataka = new ArrayList();
                            if (line.matches("\\d+,.*")) {
                                linijaPodataka = Collections.list(new StringTokenizer(line, ",", false)).stream().map(token -> Double.parseDouble((String) token)).collect(Collectors.toList());
                                voltage = linijaPodataka.get(0);
                                current = linijaPodataka.get(1);
                                seconds = linijaPodataka.get(2);

                                voltageData.getData().add(new XYChart.Data(voltage, seconds));
                                currentData.getData().add(new XYChart.Data(current, seconds));

//                                System.out.println("" + voltage + ", " + current + ", " + seconds);
                            }
                            linijaPodataka.clear();
                        } catch (Exception ex) {
                            System.out.println("Something is wrong with parsing!");
                        }
                    }
                    scanner.close();
                }
            };

        } else {
            graph.getData().clear();
            chosenPort.closePort();
            selectPortDrop.setDisable(false);
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll(dropItems);
    }

}
