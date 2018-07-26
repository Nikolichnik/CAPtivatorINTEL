package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import com.fazecast.jSerialComm.SerialPort;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
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
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUIController implements Initializable {

    private final FileReader fileReader = new FileReader();
    private FileWriter fileWriter;
    private FileWriter fileWriterAll;
    private final CommHandler comms = new CommHandler();
    private SerialPort chosenPort = null;

    private final File folderRaw = new File("data/raw/");
    private final File folderData = new File("data/");

    private final XYChart.Series voltageData = new XYChart.Series();
    private final XYChart.Series currentData = new XYChart.Series();

    private final XYChart.Series voltageDataFile = new XYChart.Series();
    private final XYChart.Series currentDataFile = new XYChart.Series();

    private final XYChart.Series stats = new XYChart.Series();
    private final XYChart.Series statsDates = new XYChart.Series();

    private int voltage = 0;
    private int current = 0;
    private int seconds = 0;

    private Task task;

    private int cycle = 0;
    private int cycleAll = 0;
    private Double measuredCapacity = -1.0;

    double xOffset;
    double yOffset;

    boolean confirm = true;

    @FXML
    private LineChart<?, ?> graphSerial, graphFile, graphStats, graphStatsDates;

    @FXML
    private JFXComboBox<String> selectFileDrop, selectPortDrop;
    ObservableList<String> selectFileDropItems;

    @FXML
    ObservableList<String> selectPortDropItems = comms.getPortList();

    @FXML
    private JFXButton minimiseButton, maximiseButton, closeButton, connectButton, serialReadButton, fileReadButton, statsReadButton, readFileButton;

    @FXML
    private TextField capacitorIDTextBox;

    @FXML
    private VBox readStatsVBox, readFileVBox, readFromSerialVBox;

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
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    public void move(MouseEvent event) {
        Stage stage = (Stage) maximiseButton.getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    public void handleSideButtonsClick(ActionEvent event) {
        if (event.getSource() == serialReadButton) {
            readFromSerialVBox.toFront();
        } else if (event.getSource() == fileReadButton) {
            readFileVBox.toFront();
        } else if (event.getSource() == statsReadButton) {
            readStatsVBox.toFront();
        }
    }

    public void handleConnectClick() {
        if (selectPortDrop.getValue() == null) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("COM port missing!");
            alert.setContentText("Please select port in order to continue.");
            alert.showAndWait();
            confirm = false;
        } else {
            confirm = true;
        }

        if (connectButton.getText().equalsIgnoreCase("Connect") && confirm) {
            if (capacitorIDTextBox.getText().trim().isEmpty()) {
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText("Capacitor ID missing!");
                alert.setContentText("Are you sure you want to proceed without recording data?");
                ButtonType yes = new ButtonType("Yes");
                ButtonType cancel = new ButtonType("Cancel");
                alert.getButtonTypes().setAll(yes, cancel);
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == cancel) {
                    resetConnect();
                } else {
                    fileWriter = null;
                    confirm = true;
                }
            }
            if (confirm) {
                task = new Task<Void>() {
                    @Override
                    public Void call() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                connectButton.setText("Connected");
                                connectButton.setStyle("-fx-background-color: #7C3034; -fx-text-fill: #DBDBDB;");
                                selectPortDrop.setDisable(true);
                            }
                        });
                        comms.setChosenPort(SerialPort.getCommPort(selectPortDrop.getValue()));
                        chosenPort = comms.getChosenPort();
                        chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
                        chosenPort.openPort();
                        confirm = true;
                        try (Scanner scanner = new Scanner(chosenPort.getInputStream())) {
                            List<Integer> linijaPodataka;
                            while (scanner.hasNextLine() && confirm && !isCancelled()) {
                                try {
                                    String line = scanner.nextLine();
                                    if (line.contains("Discharging...") && !capacitorIDTextBox.getText().trim().isEmpty()) {
                                        LocalDateTime timestamp = LocalDateTime.now();
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                                        String fileName = "data/raw/" + capacitorIDTextBox.getText() + "_" + ++cycle + "_" + dtf.format(timestamp) + ".txt";
                                        System.out.println(fileName);
                                        try {
                                            fileWriter = new FileWriter(fileName, false);
                                        } catch (Exception ex) {
                                            System.out.println("Unable to create file!");
                                        }
                                    }
                                    if ((line.contains("Discharge cycle") || line.contains("Charge cycle")) && !capacitorIDTextBox.getText().trim().isEmpty()) {
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
                                        String lastLine;
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
                                                currentData.getData().add(new XYChart.Data(seconds, current));//                                            
                                            }
                                        });
                                    } else {
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                voltageData.getData().clear();
                                                currentData.getData().clear();
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
                resetConnect();
            }
        } else {
            resetConnect();
        }
    }

    public void resetConnect() {
        if (chosenPort != null) {
            chosenPort.closePort();
        }
        confirm = false;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                voltageData.getData().clear();
                currentData.getData().clear();
                selectPortDrop.setDisable(false);
                connectButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #323232;");
                connectButton.setText("Connect");
            }
        });
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
                        while (scanner.hasNextLine() && !isCancelled()) {
                            try {
                                String line = scanner.nextLine();
                                linijaPodataka = Collections.list(new StringTokenizer(line, ",", false)).stream().map(token -> Integer.parseInt((String) token)).collect(Collectors.toList());
                                voltage = linijaPodataka.get(0);
                                current = linijaPodataka.get(1);
                                seconds = linijaPodataka.get(2);
                                linijaPodataka.clear();
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        voltageDataFile.getData().add(new XYChart.Data(seconds, voltage));
                                        currentDataFile.getData().add(new XYChart.Data(seconds, current));
                                    }
                                });
                            } catch (Exception ex) {
                                System.out.println("Something is wrong with parsing!");
                            }
                        }
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
            System.out.println("Entered else for file button");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    voltageDataFile.getData().clear();
                    currentDataFile.getData().clear();
                }
            });
            readFileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #323232;");
            readFileButton.setText("Read file");
        }
    }

    public void handleStartMeasurementButton() {

    }

    public void handlePauseMeasurementButton() {

    }

    public void handleSelectPortDropClick() {
//        task = new Task<Void>() {
//            @Override
//            public Void call() {
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        selectPortDropItems = comms.getPortList();
//                        if (selectPortDropItems.size() != selectPortDrop.getItems().size()) {
//                            selectPortDrop.getItems().clear();
//                            selectPortDrop.getItems().addAll(selectPortDropItems);
//                        }
//                    }
//                });
//                return null;
//            }
//        };
//        Thread th = new Thread(task);
//        th.setDaemon(true);
//        th.start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectPortDrop.getItems().clear();
        selectPortDrop.getItems().addAll(selectPortDropItems);

        graphSerial.setCreateSymbols(false);
        graphSerial.setAnimated(false);
        graphSerial.getXAxis().setLabel("t [s]");
        graphSerial.getYAxis().setLabel("I/U [mA/mV]");
        graphSerial.setLegendSide(Side.BOTTOM);
        voltageData.setName("Voltage");
        currentData.setName("Current");
        graphSerial.getData().addAll(voltageData, currentData);

        graphFile.setCreateSymbols(false);
        graphFile.setAnimated(false);
        graphFile.getXAxis().setLabel("t [s]");
        graphFile.getYAxis().setLabel("I/U [mA/mV]");
        graphFile.setLegendSide(Side.BOTTOM);
        voltageDataFile.setName("Voltage");
        currentDataFile.setName("Current");
        graphFile.getData().addAll(voltageDataFile, currentDataFile);

        graphStats.setCreateSymbols(false);
        graphStats.setAnimated(false);
        graphStats.getXAxis().setLabel("Number of cycles");
        graphStats.getYAxis().setLabel("C [F]");
        graphStats.setLegendVisible(false);
        graphStats.getData().addAll(stats);

//        graphStatsDates.setCreateSymbols(false);
//        graphStatsDates.setAnimated(false);
//        graphStatsDates.getXAxis().setLabel("Number of cycles");
//        graphStatsDates.getYAxis().setLabel("C [F]");
//        graphStats.setLegendVisible(false);
//        graphStatsDates.getData().addAll(statsDates);
        
        selectFileDropItems = fileReader.getFileRawList(folderRaw);
        selectFileDrop.getItems().addAll(selectFileDropItems);
    }

}
