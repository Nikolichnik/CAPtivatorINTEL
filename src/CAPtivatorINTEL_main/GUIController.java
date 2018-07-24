package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import com.fazecast.jSerialComm.SerialPort;
import comms.CommHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
    private FileWriter fileWriter;
    private FileWriter fileWriterAll;
    private final CommHandler comms = new CommHandler();
    private SerialPort chosenPort;
    
    private File folderRaw = new File("data/raw/");
    private File folderData = new File("data/");

    private final XYChart.Series voltageData = new XYChart.Series();
    private final XYChart.Series currentData = new XYChart.Series();

    private int voltage = 0;
    private int current = 0;
    private int seconds = 0;

    private Task task;

    private int cycle = 0;
    private int cycleAll = 0;
    private Double measuredCapacity = -1.0;

    @FXML
    private Region leftPanel;

    @FXML
    private LineChart<?, ?> graph;

    @FXML
    private ComboBox<String> selectFileDrop;    
    ObservableList<String> selectFileDropItems;

    @FXML
    private ToggleButton readFileButton;

    @FXML
    private Region bottomPanel;

    @FXML
    private TextField capacitorIDTextBox;

    @FXML
    private CheckBox writeFileCheck;

    @FXML
    private ComboBox<String> selectPortDrop;
    ObservableList<String> selectPortDropItems = comms.getPortList();

    @FXML
    private ToggleButton connectButton;

    public void handleConnectClick() {

        if (connectButton.getText().equalsIgnoreCase("Connect")) {
            comms.setChosenPort(SerialPort.getCommPort(selectPortDrop.getValue()));
            chosenPort = comms.getChosenPort();
            chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
            if (chosenPort.openPort()) {
                connectButton.setText("Connected");
                connectButton.setStyle("-fx-background-color: #7C3034; -fx-text-fill: #DBDBDB;");
                selectPortDrop.setDisable(true);
            }

            task = new Task<Void>() {
                @Override
                public Void call() {
                    try (Scanner scanner = new Scanner(chosenPort.getInputStream())) {
                        List<Integer> linijaPodataka;
                        while (scanner.hasNextLine()) {
                            if (isCancelled()) {
                                break;
                            }
                            try {
                                String line = scanner.nextLine();
//                            System.out.println(line);
                                if (writeFileCheck.isSelected() && capacitorIDTextBox.getText() != null) {
                                    if (line.contains("Discharging...")) {
                                        LocalDateTime timestamp = LocalDateTime.now();
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
//                                        System.out.println(dtf.format(timestamp));
                                        String fileName = "data/raw/" + capacitorIDTextBox.getText() + "_" + ++cycle + "_" + dtf.format(timestamp) + ".txt";
                                        System.out.println(fileName);
                                        try {
                                            fileWriter = new FileWriter(fileName, false);
                                        } catch (Exception ex) {
                                            System.out.println("Unable to create file!");
                                        }
                                    }
                                    if (line.contains("Discharge cycle") || line.contains("Charge cycle")) {
                                        LocalDateTime timestamp = LocalDateTime.now();
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                                        String fileName = "data/" + capacitorIDTextBox.getText() + "_all" + ".txt";
                                        try {
                                            fileWriterAll = new FileWriter(fileName, true);
                                        } catch (Exception ex) {
                                            System.out.println("Unable to create file!");
                                        }
                                        FileInputStream in = new FileInputStream(fileName);
                                        BufferedReader br = new BufferedReader(new InputStreamReader(in));
                                        String strLine = null, tmp;
                                        while ((tmp = br.readLine()) != null) {
                                            strLine = tmp;
                                        }
                                        String lastLine = null;

                                        if (strLine != null) {
                                            lastLine = strLine.substring(strLine.indexOf(",") + 1, strLine.indexOf(",", strLine.indexOf(",") + 1));
                                        } else {
                                            lastLine = "0";
                                        }
                                        System.out.println(lastLine);
                                        in.close();

                                        cycleAll = Integer.parseInt(lastLine);

                                        measuredCapacity = Double.parseDouble(line.substring(line.indexOf("=") + 2));

                                        if (line.contains("Discharge cycle")) {
                                            cycleAll++;
                                        }

                                        String data = capacitorIDTextBox.getText() + "," + cycleAll + "," + dtf.format(timestamp) + "," + measuredCapacity;

                                        fileWriterAll.append(data + "\r\n");
                                        fileWriterAll.flush();
                                    }
                                    if (line.contains("Measurement complete!")) {
                                        fileWriter = null;
                                        fileWriterAll = null;
                                    }
                                }
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
//                                            capacitorIDTextBox.setText(getMessage());
                                        }
                                    });
                                }
                                if (fileWriter != null) {
                                    fileWriter.append(voltage + "," + current + "," + seconds + "\r\n");
                                    fileWriter.flush();
                                }
                            } catch (Exception ex) {
                                System.out.println("Something is wrong with parsing!");
                            }
                        }
                    }

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
            connectButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #323232;");
            connectButton.setText("Connect");
        }
    }

    public void handleFileReadClick() {
//        File file = null;
//        Scanner sc = null;
//        try {
//            file = new File("data/raw/" + selectFileDrop.getValue());
//            sc = new Scanner(file);
//        } catch (Exception ex) {
//            System.out.println("Waaa!");
//        }
//        if (readFileButton.getText().equalsIgnoreCase("Read file")) {
//            readFileButton.setText("Reading file...");
//            readFileButton.setStyle("-fx-background-color: #7C3034; -fx-text-fill: #DBDBDB;");
//
//        } else {
//            readFileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #323232;");
//            readFileButton.setText("Read file");
//        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources
    ) {

        final String IDLE_BUTTON_STYLE = "-fx-background-color: transparent;";
        final String HOVERED_BUTTON_STYLE = "-fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;";

        connectButton.setStyle(IDLE_BUTTON_STYLE);
//        connectButton.setOnMouseEntered(e -> connectButton.setStyle(HOVERED_BUTTON_STYLE));
//        connectButton.setOnMouseExited(e -> connectButton.setStyle(IDLE_BUTTON_STYLE));

        readFileButton.setStyle(IDLE_BUTTON_STYLE);
//        readFileButton.setOnMouseEntered(e -> readFileButton.setStyle(HOVERED_BUTTON_STYLE));
//        readFileButton.setOnMouseExited(e -> readFileButton.setStyle(IDLE_BUTTON_STYLE));

        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll(selectPortDropItems);

        selectFileDropItems = fileReader.getFileRawList(folderRaw);
        
        selectFileDrop.getItems().clear();
        selectFileDrop.getItems().addAll(selectFileDropItems);

        graph.setCreateSymbols(false);
        graph.setAnimated(false);

        voltageData.setName("Voltage");
        currentData.setName("Current");

        graph.getData().addAll(voltageData, currentData);
    }

}
