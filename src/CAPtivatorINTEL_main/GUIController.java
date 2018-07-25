package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import com.fazecast.jSerialComm.SerialPort;
import com.jfoenix.controls.JFXButton;
import comms.CommHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class GUIController implements Initializable {

    private final FileReader fileReader = new FileReader();
    private FileWriter fileWriter;
    private FileWriter fileWriterAll;
    private final CommHandler comms = new CommHandler();
    private SerialPort chosenPort;

    private final File folderRaw = new File("data/raw/");
    private final File folderData = new File("data/");

    private final XYChart.Series voltageData = new XYChart.Series();
    private final XYChart.Series currentData = new XYChart.Series();

    private int voltage = 0;
    private int current = 0;
    private int seconds = 0;

    private Task task;

    private int cycle = 0;
    private int cycleAll = 0;
    private Double measuredCapacity = -1.0;

    double xOffset;
    double yOffset;

    @FXML
    private LineChart<?, ?> graph;

    @FXML
    private ComboBox<String> selectFileDrop;
    ObservableList<String> selectFileDropItems;

    @FXML
    private ToggleButton readFileButton;

    @FXML
    private JFXButton minimiseButton, maximiseButton, closeButton;

    @FXML
    private TextField capacitorIDTextBox;

    @FXML
    private CheckBox writeFileCheck;

    @FXML
    private ComboBox<String> selectPortDrop;
    ObservableList<String> selectPortDropItems = comms.getPortList();

    @FXML
    private ToggleButton connectButton;

    @FXML
    private HBox topBar;

    public void handleMinimiseButton() {
        Stage stage = (Stage) minimiseButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void handleMaximiseButton() {
        Stage stage = (Stage) maximiseButton.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            stage.setMaximized(true);
        }
    }

    public void handleCloseButton() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    public void resize(MouseEvent event) {
        Stage stage = (Stage) maximiseButton.getScene().getWindow();
        double newX = event.getScreenX() - stage.getX() + 13;
        double newY = event.getScreenY() - stage.getY() + 10;
        if (newX % 5 == 0 || newY % 5 == 0) {
            if (newX > 550) {
                stage.setWidth(newX);
            } else {
                stage.setWidth(550);
            }

            if (newY > 200) {
                stage.setHeight(newY);
            } else {
                stage.setHeight(200);
            }
        }
    }

    public void movePressed(MouseEvent event) {

        Stage stage = (Stage) maximiseButton.getScene().getWindow();

        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    
    public void move(MouseEvent event) {

        Stage stage = (Stage) maximiseButton.getScene().getWindow();       

        System.out.println(" xxxDragged " + xOffset + " " + yOffset);

        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }    

    public void handleReadSerialButtonClick(ActionEvent event) {
        try {
            Parent readSerialParent = FXMLLoader.load(getClass().getResource("/CAPtivatorINTEL_main/FXMLDocument.fxml"));
            Scene readSerialScene = new Scene(readSerialParent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            readSerialScene.setRoot(readSerialParent);

            stage.setScene(readSerialScene);
            stage.show();

        } catch (IOException ex) {
            System.out.println("FXMLLoader was NOT successful!");
        }
    }

    public void handleReadFileButtonClick(ActionEvent event) {
        try {
            Parent readFileParent = FXMLLoader.load(getClass().getResource("/CAPtivatorINTEL_main/FXMLDocument.fxml"));
            Scene readFileScene = new Scene(readFileParent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(readFileScene);
            stage.show();

        } catch (IOException ex) {
            System.out.println("FXMLLoader was NOT successful!");
        }
    }

    public void handleReadStatsButtonClick(ActionEvent event) {
        try {
            Parent readStatsParent = FXMLLoader.load(getClass().getResource("/CAPtivatorINTEL_main/FXMLDocument.fxml"));
            Scene readStatsScene = new Scene(readStatsParent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(readStatsScene);
            stage.show();

        } catch (IOException ex) {
            System.out.println("FXMLLoader was NOT successful!");
        }
    }

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
        if (readFileButton.getText().equalsIgnoreCase("Read file") && selectFileDrop.getValue() != null) {
            readFileButton.setText("Reading file...");
            readFileButton.setStyle("-fx-background-color: #7C3034; -fx-text-fill: #DBDBDB;");
            task = new Task<Void>() {
                @Override
                public Void call() {
                    try (Scanner scanner = new Scanner(new File("data/raw/" + selectFileDrop.getValue()));) {
                        List<Integer> linijaPodataka;
                        while (scanner.hasNextLine()) {
                            if (isCancelled()) {
                                break;
                            }
                            try {
                                String line = scanner.nextLine();
//                            System.out.println(line);
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
                                    }
                                });
                            } catch (Exception ex) {
                                System.out.println("Something is wrong with parsing!");
                            }
                        }
//                        readFileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #323232;");
//                        readFileButton.setText("Read file");
                    } catch (Exception ex) {
                        System.out.println("File not found!");
                    }
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        } else {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    voltageData.getData().clear();
                    currentData.getData().clear();
                }
            });
            readFileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #323232;");
            readFileButton.setText("Read file");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources
    ) {

        final String IDLE_BUTTON_STYLE = "-fx-background-color: transparent;";
        final String HOVERED_BUTTON_STYLE = "-fx-shadow-highlight-color, -fx-outer-border, -fx-inner-border, -fx-body-color;";

//        connectButton.setStyle(IDLE_BUTTON_STYLE);
//        connectButton.setOnMouseEntered(e -> connectButton.setStyle(HOVERED_BUTTON_STYLE));
//        connectButton.setOnMouseExited(e -> connectButton.setStyle(IDLE_BUTTON_STYLE));
//        readFileButton.setStyle(IDLE_BUTTON_STYLE);
//        readFileButton.setOnMouseEntered(e -> readFileButton.setStyle(HOVERED_BUTTON_STYLE));
//        readFileButton.setOnMouseExited(e -> readFileButton.setStyle(IDLE_BUTTON_STYLE));
        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll(selectPortDropItems);

        selectFileDropItems = fileReader.getFileRawList(folderRaw);

        selectFileDrop.getItems().clear();
        selectFileDrop.getItems().addAll(selectFileDropItems);

//        graph.setCreateSymbols(false);
//        graph.setAnimated(false);
        voltageData.setName("Voltage");
        currentData.setName("Current");

//        graph.getData().addAll(voltageData, currentData);
    }

}
