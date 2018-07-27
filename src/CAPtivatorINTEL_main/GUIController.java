package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import com.fazecast.jSerialComm.SerialPort;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import comms.CommHandler;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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

    private final XYChart.Series stats = new XYChart.Series();
    private final XYChart.Series statsDates = new XYChart.Series();

    private int voltage = 0, current = 0, seconds = 0;

    private Task task;

    private int cycle = 0;
    private int cycleAll = 0;
    private Double measuredCapacity = -1.0;

    private double xOffset, yOffset, cVoltage = 2.7;

    private boolean confirm = true;

    @FXML
    private LineChart<?, ?> graphSerial, graphFile, graphStats, graphStatsDates;

    @FXML
    private JFXComboBox<String> selectPortDrop, selectCapacitorDrop, selectSessionDrop;
    ObservableList<String> selectPortDropItems, selectCapacitorDropItems, selectSessionDropItems;

    @FXML
    private JFXButton minimiseButton, maximiseButton, closeButton,
            startMeasurementButton, pauseMeasurementButton, connectButton,
            serialReadButton, fileReadButton, statsReadButton,
            addFileButton, removeFileButton, removeAllFilesButton;

    @FXML
    private TextField capacitorIDTextBox;

    @FXML
    private VBox readStatsVBox, readFileVBox, readFromSerialVBox;

    @FXML
    private HBox fileCardsStack, dataCardsStack;

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
            serialReadButton.setStyle("-fx-background-color: #5A2728;");
            fileReadButton.setStyle("-fx-background-color: transparent;");
            statsReadButton.setStyle("-fx-background-color: transparent;");
            serialReadButton.setOnMouseExited(e -> serialReadButton.setStyle("-fx-background-color: #5A2728;"));
            fileReadButton.setOnMouseExited(e -> fileReadButton.setStyle("-fx-background-color: transparent;"));
            statsReadButton.setOnMouseExited(e -> statsReadButton.setStyle("-fx-background-color: transparent;"));
        } else if (event.getSource() == fileReadButton) {
            readFileVBox.toFront();
            serialReadButton.setStyle("-fx-background-color: transparent;");
            fileReadButton.setStyle("-fx-background-color: #5A2728;");
            statsReadButton.setStyle("-fx-background-color: transparent;");
            serialReadButton.setOnMouseExited(e -> serialReadButton.setStyle("-fx-background-color: transparent;"));
            fileReadButton.setOnMouseExited(e -> fileReadButton.setStyle("-fx-background-color: #5A2728;"));
            statsReadButton.setOnMouseExited(e -> statsReadButton.setStyle("-fx-background-color: transparent;"));
        } else if (event.getSource() == statsReadButton) {
            readStatsVBox.toFront();
            serialReadButton.setStyle("-fx-background-color: transparent;");
            fileReadButton.setStyle("-fx-background-color: transparent;");
            statsReadButton.setStyle("-fx-background-color: #5A2728;");
            serialReadButton.setOnMouseExited(e -> serialReadButton.setStyle("-fx-background-color: transparent;"));
            fileReadButton.setOnMouseExited(e -> fileReadButton.setStyle("-fx-background-color: transparent;"));
            statsReadButton.setOnMouseExited(e -> statsReadButton.setStyle("-fx-background-color: #5A2728;"));
        }
    }

    public void handleConnectClick() {

        String fileNameAll = "data/" + capacitorIDTextBox.getText() + "_all" + ".txt";   // Possibly move out of thread
        try {
            fileWriterAll = new FileWriter(fileNameAll, true);
        } catch (Exception ex) {
            System.out.println("Unable to create file!");
        }

        List<String> listOfFiles = fileReader.getFileRawList(folderData);
        boolean virgin = false;

        for (String file : listOfFiles) {
            if (file.contains(capacitorIDTextBox.getText())) {
                virgin = true;
            }
        }

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
            } else if (virgin) {                                                // If new capacitor iD
                System.out.println("Entered new ID");
                LocalDateTime timestamp = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Initial setup");
                dialog.setHeaderText("New capacitor ID detected!");
                dialog.setContentText("Please enter nominal capacity:");
                Optional<String> result = dialog.showAndWait();

                double nominalCapacity = 0.0;

                if (result.isPresent()) {
                    nominalCapacity = Double.parseDouble(result.get());
                }
                try {
                    fileWriterAll.append("#," + dtf.format(timestamp) + "," + nominalCapacity + "\r\n");
                    fileWriterAll.flush();
                } catch (IOException ex) {
                    System.out.println("Initial value NOT set!");
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
                                        String fileName = "data/raw/" + capacitorIDTextBox.getText() + "_" + dtf.format(timestamp) + "_" + ++cycle + ".txt";
                                        try {
                                            fileWriter = new FileWriter(fileName, false);
                                        } catch (Exception ex) {
                                            System.out.println("Unable to create file!");
                                        }
                                    }
                                    if ((line.contains("Discharge cycle") || line.contains("Charge cycle")) && !capacitorIDTextBox.getText().trim().isEmpty()) {
                                        LocalDateTime timestamp = LocalDateTime.now();
                                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
                                        ///
                                        FileInputStream in = new FileInputStream(fileNameAll);
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
                                        String data = cycleAll + "," + dtf.format(timestamp) + "," + measuredCapacity;
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
    } // Add check if cID is entered for the first time and prompt for nominal C value

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

    public void handleStartMeasurementButton() {

    }

    public void handlePauseMeasurementButton() {

    }

    public void handleSelectPortDropClick() {
        selectPortDropItems = comms.getPortList();
        if (selectPortDropItems.size() != selectPortDrop.getItems().size()) {
            task = new Task<Void>() {
                @Override
                public Void call() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            selectPortDrop.getItems().clear();
                            selectPortDrop.getItems().addAll(selectPortDropItems);
                        }
                    });
                    return null;
                }

            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        }
    }

    public void handleSelectCapacitorDropClick() {
        final List<String> files = new LinkedList(fileReader.getFileRawList(folderRaw));
        if (files.size() != selectCapacitorDrop.getItems().size()) {
            task = new Task<Void>() {
                @Override
                public Void call() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            selectCapacitorDropItems.clear();
                            for (String file : files) {
                                selectCapacitorDropItems.add(file.substring(0, file.indexOf("_")));
                            }
                            selectCapacitorDrop.getItems().addAll(selectCapacitorDropItems);
                        }
                    });
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        }
    }

    public void handleSelectSessionDropClick() {
        if (selectCapacitorDrop.getValue() != null) {
            final List<String> files = new LinkedList(fileReader.getFileRawList(folderRaw));
            task = new Task<Void>() {
                @Override
                public Void call() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            String item = "";
                            DateFormat dtfIn = new SimpleDateFormat("yyyyMMddHHmm");
                            DateFormat dtfOut = new SimpleDateFormat("dd/MM | HH:mm");
                            selectSessionDropItems.clear();
                            for (String file : files) {
                                if (file.contains(selectCapacitorDrop.getValue())) {
                                    item = file.substring(file.indexOf("_", file.indexOf("_") + 1) + 1, file.indexOf("."));
                                    Date timestamp = null;
                                    try {
                                        timestamp = dtfIn.parse(item);
                                    } catch (ParseException ex) {
                                        System.out.println("File name format incorrect!");
                                    }
                                    selectSessionDropItems.add(dtfOut.format(timestamp) + " | " + file.substring(file.indexOf("_") + 1, file.indexOf("_", file.indexOf("_") + 1)));
                                }
                            }
                            selectSessionDrop.getItems().clear();
                            selectSessionDrop.getItems().addAll(selectSessionDropItems);
                        }
                    });
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("Capacitor ID not selected!");
            alert.setContentText("Please select capacitor ID in order to continue.");
            alert.showAndWait();
        }
    }

    public void handleAddFileClick() {
        if (selectSessionDrop.getValue() != null) {
            DateFormat dtfIn = new SimpleDateFormat("dd/MM | HH:mm");
            DateFormat dtfOut = new SimpleDateFormat("MMddHHmm");
            task = new Task<Void>() {
                @Override
                public Void call() {
                    String item = selectSessionDrop.getValue();
                    item = item.substring(0, item.indexOf("|", item.indexOf("|") + 1)).trim();
                    Date timestampIn = null;
                    try {
                        timestampIn = dtfIn.parse(item);
                    } catch (ParseException ex) {
                        System.out.println("Unlikely case of parsing date exception :)");;
                    }
                    item = dtfOut.format(timestampIn);
                    String address = "data/raw/";
                    for (String file : fileReader.getFileRawList(folderRaw)) {
                        if (file.contains(selectCapacitorDrop.getValue()) && file.contains(item)) {
                            address += file;
                        }
                    }
                    XYChart.Series voltageDataFile = new XYChart.Series();
                    voltageDataFile.setName(selectCapacitorDrop.getValue() + "_" + selectSessionDrop.getValue() + "_U");
                    XYChart.Series currentDataFile = new XYChart.Series();
                    currentDataFile.setName(selectCapacitorDrop.getValue() + "_" + selectSessionDrop.getValue() + "_I");
                    try (Scanner scanner = new Scanner(new File(address));) {
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
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            graphFile.getData().addAll(voltageDataFile, currentDataFile);
                        }
                    });
                    return null;
                }
            };
            Thread th = new Thread(task);
            th.setDaemon(true);
            th.start();
        } else {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning!");
            alert.setHeaderText("Session not selected!");
            alert.setContentText("Please select session in order to continue.");
            alert.showAndWait();
        }
    }

    public void handleRemoveFileClick() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphFile.getData().removeIf((XYChart.Series data) -> data.getName().contains(selectCapacitorDrop.getValue() + "_" + selectSessionDrop.getValue()));
            }
        });
    }

    public void handleRemoveAllFilesClick() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphFile.getData().clear();
            }
        });
    }

    public void handleSelectStatsDrop() {

    }

    public void handleAddStatsButtonClick() {

    }

    public void handleClearStatsButtonClick() {

    }

    public void handleClearAllStatsButtonClick() {

    }

    public VBox createDataCard(String cID, String cTimestamp, boolean file) {
        int cNominal = 0, cInitial = 0, cMeasured = 0, percentNominal = 0, percentInitial = 0, vBoxWidth = 90, titleHeight = 21, dataFieldHeight = 13;
        int Cmin = 0, Cmiddle = 0, Cmax = 0, Imin = 0, Imiddle = 0, Imax = 0;
        double Qmin = 0, Qmiddle = 0, Qmax = 0, Emin = 0, Emiddle = 0, Emax = 0;

        String addressData = "data/" + cID + "_all.txt";
        String addressRaw = "data/raw/";

        String dateStart, dateLast, days;

        DateFormat dtfIn = new SimpleDateFormat("YYYYMMddHHmm");
        DateFormat dtfOut = new SimpleDateFormat("dd/MM");
        Date dateStartRaw = new Date();
        Date dateLastRaw = new Date();

        for (String fileRaw : fileReader.getFileRawList(folderRaw)) {
            if (fileRaw.contains(cID) && fileRaw.contains(cTimestamp)) {
                addressRaw += fileRaw;
            }
        }

        List<Integer> capacitances = new LinkedList();
        List<String> dates = new LinkedList();
        try (Scanner scanner = new Scanner(new File(addressData));) {
            while (scanner.hasNextLine()) {
                if (!scanner.nextLine().contains("#")) {
                    capacitances.add(Collections.list(new StringTokenizer(scanner.nextLine(), ",", false)).stream().map(token -> Integer.parseInt((String) token)).collect(Collectors.toList()).get(2));
                    dates.add(Collections.list(new StringTokenizer(scanner.nextLine(), ",", false)).stream().map(token -> (String) token).collect(Collectors.toList()).get(1));
                } else {
                    cNominal = Integer.parseInt(scanner.nextLine().substring(scanner.nextLine().lastIndexOf(",") + 1, scanner.nextLine().length() - 1));
                }
                if (scanner.nextLine().contains(cTimestamp)) {
                    cMeasured = Integer.parseInt(scanner.nextLine().substring(scanner.nextLine().lastIndexOf(",") + 1, scanner.nextLine().length() - 1));
                }
            }
            cInitial = capacitances.get(0);

            if (!file) {
                int sum = 0;
                for (Integer capacitance : capacitances) {
                    sum += capacitance;
                }
                Cmiddle = sum / capacitances.size();
            } else {
                Cmiddle = cMeasured;
            }
            Collections.sort(capacitances);

            Cmin = capacitances.get(0);
            Cmax = capacitances.get(capacitances.size() - 1);

            try {
                dateStartRaw = dtfIn.parse(dates.get(0));
                dateLastRaw = dtfIn.parse(dates.get(dates.size() - 1));
            } catch (ParseException ex) {
                System.out.println("Parsing dates broke!");
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Something wrong with file or parsing while making dataCard!");
        }

        List<Integer> currents = new LinkedList();
        if (file) {
            try (Scanner scanner = new Scanner(new File(addressRaw));) {
                while (scanner.hasNextLine()) {
                    currents.add(Collections.list(new StringTokenizer(scanner.nextLine(), ",", false)).stream().map(token -> Integer.parseInt((String) token)).collect(Collectors.toList()).get(2));
                }
                int sum = 0;
                for (Integer curr : currents) {
                    sum += curr;
                }
                Imiddle = sum / currents.size();

                Collections.sort(currents);

                Imin = currents.get(0);
                Imax = currents.get(currents.size() - 1);

            } catch (FileNotFoundException ex) {
                System.out.println("Raw file not found!");
            }
        }

        if (Cmin != 0) { // 'cause if one is set, all are.
            Qmin = 0.5 * Cmin * cVoltage;
            Qmiddle = 0.5 * Cmiddle * cVoltage;
            Qmax = 0.5 * Cmax * cVoltage;

            Emin = 0.5 * Cmin * cVoltage;
            Emiddle = 0.5 * Cmiddle * cVoltage;
            Emax = 0.5 * Cmax * cVoltage;
        }

        VBox dataCard = new VBox();
        dataCard.setPrefSize(vBoxWidth, 2000);
        dataCard.setMinWidth(vBoxWidth);
        dataCard.setMaxWidth(vBoxWidth);

        Label title = new Label(cID);
        if (file) {
            title.setText(cID + " | " + cTimestamp);
        }
        title.setMinSize(vBoxWidth, titleHeight);
        title.setMaxSize(vBoxWidth, titleHeight);
        title.setAlignment(Pos.CENTER);

        ObservableList<PieChart.Data> doughnutNominalData = FXCollections.observableArrayList();
        ObservableList<PieChart.Data> doughnutInitialData = FXCollections.observableArrayList();

        DoughnutChart doughnutNominal = new DoughnutChart(doughnutNominalData);
        DoughnutChart doughnutInitial = new DoughnutChart(doughnutInitialData);

//        PieChart.Data doughnutNominalChartData = new PieChart.Data("% of nominal", percentNominal);
//        PieChart.Data doughnutInitialChartData = new PieChart.Data("% of initial", percentInitial);

        VBox cellCapacitance = createDataCell("C[F]", Cmin, Cmiddle, Cmax);
        VBox cellQ = createDataCell("Q[J]", Qmin, Qmiddle, Qmax);
        VBox cellEnergy = createDataCell("E[J]", Emin, Emiddle, Emax);

        dataCard.getChildren().addAll(title, doughnutNominal, doughnutInitial, cellCapacitance);

        if (file) {
            VBox cellCurrent = createDataCell("I[mA]", Imin, Imiddle, Imax);
            dataCard.getChildren().addAll(cellCurrent);
        }

        dataCard.getChildren().addAll(cellQ, cellEnergy);
        return dataCard;
    }

    public VBox createDataCell(String title, double leftValue, double middleValue, double rightValue) {
        VBox dataCell = new VBox();
        dataCell.setMaxSize(90, 70);
        dataCell.setMinSize(90, 70);

        Label cellTitle = new Label(title);
        cellTitle.setAlignment(Pos.CENTER);
        cellTitle.setMaxHeight(20);
        cellTitle.setMinHeight(20);

        HBox values = new HBox();

        String leftString = String.valueOf(leftValue);
        String middleString = String.valueOf(middleValue);
        String rightString = String.valueOf(rightValue);

        Label left = new Label(leftString);
        left.setAlignment(Pos.CENTER);
        Label middle = new Label(middleString);
        middle.setAlignment(Pos.CENTER);
        Label right = new Label(rightString);
        right.setAlignment(Pos.CENTER);

        values.getChildren().addAll(left, middle, right);

        dataCell.getChildren().addAll(cellTitle, values);

        return dataCell;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        serialReadButton.setOnMouseEntered(e -> serialReadButton.setStyle("-fx-background-color: #5A2728;"));
        serialReadButton.setOnMouseExited(e -> serialReadButton.setStyle("-fx-background-color: transparent;"));
        fileReadButton.setOnMouseEntered(e -> fileReadButton.setStyle("-fx-background-color: #5A2728;"));
        fileReadButton.setOnMouseExited(e -> fileReadButton.setStyle("-fx-background-color: transparent;"));
        statsReadButton.setOnMouseEntered(e -> statsReadButton.setStyle("-fx-background-color: #5A2728;"));
        statsReadButton.setOnMouseExited(e -> statsReadButton.setStyle("-fx-background-color: transparent;"));

        selectPortDropItems = comms.getPortList();
        selectPortDrop.getItems().addAll(selectPortDropItems);

        selectCapacitorDropItems = FXCollections.observableArrayList();
        handleSelectCapacitorDropClick();

        selectSessionDropItems = FXCollections.observableArrayList();

        graphSerial.setCreateSymbols(false);
        graphSerial.setAnimated(false);
        graphSerial.getXAxis().setLabel("t [s]");
        graphSerial.getYAxis().setLabel("I/U [mA/mV]");
        graphSerial.setLegendSide(Side.BOTTOM);
        voltageData.setName("Voltage");
        currentData.setName("Current");
        graphSerial.getData().addAll(voltageData, currentData);

        readFromSerialVBox.toFront();
        serialReadButton.setStyle("-fx-background-color: #5A2728;");
        fileReadButton.setStyle("-fx-background-color: transparent;");
        statsReadButton.setStyle("-fx-background-color: transparent;");

        graphFile.setCreateSymbols(false);
        graphFile.setAnimated(false);
        graphFile.getXAxis().setLabel("t [s]");
        graphFile.getYAxis().setLabel("I/U [mA/mV]");
        graphFile.setLegendSide(Side.BOTTOM);
//        voltageDataFile.setName("Voltage");
//        currentDataFile.setName("Current");

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
    }

}
