package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import FileHandling.FileWriter;
import com.fazecast.jSerialComm.SerialPort;
import comms.CommHandler;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class GUIController extends Application implements Initializable {

    private Stage stage = new Stage();
    private String sceneFile = "/CAPtivatorINTEL_main/FXMLDocument.fxml";
    private URL url = getClass().getResource(sceneFile);
    private Parent root;

    private FileReader fileReader = new FileReader();
    private FileWriter fileWriter = new FileWriter();
    private CommHandler comms = new CommHandler();
    private SerialPort chosenPort;

    private XYChart.Series voltageData = new XYChart.Series();
    private XYChart.Series currentData = new XYChart.Series();

    private int voltage = 0;
    private int current = 0;
    private int seconds = 0;

    Task task;

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

    public void launchApp(String[] args) {
        this.launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(url);
            root = (Parent) loader.load(url);
//            System.out.println("  fxmlResource = " + sceneFile);
            Scene scene = new Scene(root, 900, 500);
            stage.setTitle("CAPtivatorINTEL");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.print("  * ");
            ex.printStackTrace();
            System.out.println("    ----------------------------------------\n");
        }

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

            task = new Task<Void>() {
                @Override
                public Void call() {
                    Scanner scanner = new Scanner(chosenPort.getInputStream());
                    List<Integer> linijaPodataka;
                    while (scanner.hasNextLine()) {
                        if (isCancelled()) {
                            break;
                        }
                        try {
                            String line = scanner.nextLine();
//                            System.out.println(line);
                            if (line.matches("\\d+,.*")) {
                                linijaPodataka = Collections.list(new StringTokenizer(line, ",", false)).stream().map(token -> Integer.parseInt((String) token)).collect(Collectors.toList());
                                voltage = linijaPodataka.get(0);
                                current = linijaPodataka.get(1);
                                seconds = linijaPodataka.get(2);
                                linijaPodataka.clear();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        voltageData.getData().add(new XYChart.Data(seconds, voltage));
                                        currentData.getData().add(new XYChart.Data(seconds, current));
                                        updateMessage("" + voltage + ", " + current + ", " + seconds);
                                        System.out.println(getMessage());
                                        fileNameTextBox.setText(getMessage());
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            System.out.println("Something is wrong with parsing!");
                            ex.printStackTrace();
                        }
                    }
                    scanner.close();
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        } else {
//            graph.getData().clear();
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

        final String IDLE_BUTTON_STYLE = "-fx-background-color: transparent;";
        final String HOVERED_BUTTON_STYLE = "-fx-background-color: -fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;";

        connectButton.setStyle(IDLE_BUTTON_STYLE);
        connectButton.setOnMouseEntered(e -> connectButton.setStyle(HOVERED_BUTTON_STYLE));
        connectButton.setOnMouseExited(e -> connectButton.setStyle(IDLE_BUTTON_STYLE));

        readFileButton.setStyle(IDLE_BUTTON_STYLE);
        readFileButton.setOnMouseEntered(e -> readFileButton.setStyle(HOVERED_BUTTON_STYLE));
        readFileButton.setOnMouseExited(e -> readFileButton.setStyle(IDLE_BUTTON_STYLE));

        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll(dropItems);
        graph.setCreateSymbols(false);
        graph.setAnimated(false);

        voltageData.setName("Voltage");
        currentData.setName("Current");

        graph.getData().addAll(voltageData, currentData);
    }

}
