package CAPtivatorINTEL_main;

import FileHandling.FileReader;
import com.fazecast.jSerialComm.SerialPort;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import comms.CommHandler;
import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Side;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

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

    private final XYChart.Series statsAverageCapacities = new XYChart.Series();
    private final XYChart.Series statsLastCapacities = new XYChart.Series();

    private int voltage = 0, current = 0, seconds = 0, cycle = 0, cycleAll = 0, measuredCapacity = -1, timestamp = 0;

    private Task task;

    private double xOffset, yOffset, nominalVoltage = 2.7;

    private boolean confirm = true;

    DropShadow dropShadow = new DropShadow();

    Stage stage;
    Scene scene;

    @FXML
    private LineChart<?, ?> graphSerial, graphFile, graphStats;

    @FXML
    private BarChart graphCapacities;

    @FXML
    private JFXComboBox<String> selectPortDrop, selectCapacitorDrop, selectSessionDrop, selectStatsDrop;
    ObservableList<String> selectPortDropItems, selectCapacitorDropItems, selectSessionDropItems, selectStatsDropItems;

    @FXML
    private JFXButton minimiseButton, maximiseButton, closeButton,
            startMeasurementButton, pauseMeasurementButton, connectButton,
            serialReadButton, fileReadButton, statsReadButton,
            addFileButton, removeFileButton, removeAllFilesButton,
            addStatsButton, clearStatsButton, clearAllStatsButton;

    @FXML
    private TextField capacitorIDTextBox;

    @FXML
    private VBox readStatsVBox, readFileVBox, readFromSerialVBox, dataGraphsStack;

    @FXML
    private HBox hBox, fileCardsStack, dataCardsStack;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public void handleMinimiseButton() {
        Stage stage = (Stage) minimiseButton.getScene().getWindow();
        stage.setIconified(true);
    }

    public void handleMaximiseButton() {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        Stage stage = (Stage) maximiseButton.getScene().getWindow();
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        } else {
            stage.setMaximized(true);
            stage.setX(bounds.getMinX());                                       // Workaround for undecorated maximise property to stop it from going fullscreen.
            stage.setY(bounds.getMinY());
            stage.setHeight(bounds.getHeight());
            stage.setWidth(bounds.getWidth());
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

            statsAverageCapacities.getData().clear();
            statsLastCapacities.getData().clear();

            for (String file : fileReader.getFileRawList(folderData)) {
                if (!file.contains("raw")) {
                    try (Scanner scanner = new Scanner(new File("data/" + file));) {
                        String lastLine = "";
                        int count = 0;
                        int Cmiddle = 0;
                        int Clast = 0;
                        while (scanner.hasNextLine()) {
                            lastLine = scanner.nextLine();
                            if (!lastLine.contains("#")) {
                                count++;
                                Cmiddle += Integer.parseInt(lastLine.substring(lastLine.lastIndexOf(",") + 1));
                            }
                        }
                        Clast = Integer.parseInt(lastLine.substring(lastLine.lastIndexOf(",") + 1));
                        Cmiddle /= count;

                        String capName = file.substring(0, file.indexOf("_"));

                        statsAverageCapacities.getData().add(new XYChart.Data(capName, Cmiddle));
                        statsLastCapacities.getData().add(new XYChart.Data(capName, Clast));

                    } catch (Exception ex) {
                        System.out.println("Capacitor statistic could not be generated!");
                    }
                }
            }
        }
    }

    public void handleConnectClick() {

        String fileNameAll = "data/" + capacitorIDTextBox.getText() + "_all" + ".txt";
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
                LocalDateTime timestamp = LocalDateTime.now();
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

                TextInputDialog dialog = new TextInputDialog("capacity,voltage");
                dialog.setTitle("Initial setup");
                dialog.setHeaderText("New capacitor ID detected!");
                dialog.setContentText("Please set up new capacitor:");
//                dialog
                Optional<String> result = dialog.showAndWait();

                int nominalCapacity = 0;

                if (result.isPresent()) {
                    nominalCapacity = Integer.parseInt(result.get().substring(0, result.get().indexOf(",")));
                    nominalVoltage = Double.parseDouble(result.get().substring(result.get().indexOf(",") + 1));
                }
                try {
                    fileWriterAll.append("#," + nominalVoltage + "," + nominalCapacity + "\r\n");    //dtf.format(timestamp) change to nominalVoltage
                    fileWriterAll.flush();
                } catch (IOException ex) {
                    System.out.println("Initial value NOT set!");
                }
            } else {
                try (Scanner scanner = new Scanner(new File(fileNameAll));) {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (line.contains("#")) {
                            nominalVoltage = Double.parseDouble(line.substring(line.indexOf(",") + 1, line.lastIndexOf(",")));
                        }
                    }
                } catch (FileNotFoundException ex) {
                    System.out.println("Setup file not found!");
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

                            OutputStream outputStream = chosenPort.getOutputStream();           // Communicate nominalVoltage to CAPtivatorGYM
                            PrintStream serialWriter = new PrintStream(outputStream);
                            serialWriter.println("V" + String.valueOf(nominalVoltage));
                            serialWriter.flush();

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
                                        measuredCapacity = Integer.parseInt(line.substring(line.indexOf("=") + 2));
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
                    String session = selectSessionDrop.getValue();
                    session = session.substring(0, session.indexOf("|", session.indexOf("|") + 1)).trim();
                    Date timestampIn = null;
                    try {
                        timestampIn = dtfIn.parse(session);
                    } catch (ParseException ex) {
                        System.out.println("Unlikely case of parsing date exception :)");;
                    }
                    session = dtfOut.format(timestampIn);
                    String address = "data/raw/";
                    for (String file : fileReader.getFileRawList(folderRaw)) {
                        if (file.contains(selectCapacitorDrop.getValue()) && file.contains(session)) {
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
                                voltageDataFile.getData().add(new XYChart.Data(seconds, voltage));
                                currentDataFile.getData().add(new XYChart.Data(seconds, current));
                            } catch (Exception ex) {
                                System.out.println("Something is wrong with parsing!");
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("File not found!");
                    }
                    final String sessionID = session;
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            graphFile.getData().addAll(voltageDataFile, currentDataFile);
                            fileCardsStack.getChildren().add(createDataCard(selectCapacitorDrop.getValue(), sessionID, true));
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
                int size = fileCardsStack.getChildren().size();
                for (int i = 0; i < size; i++) {
                    if (fileCardsStack.getChildren().get(i).getId().contains(selectCapacitorDrop.getValue())) {
                        fileCardsStack.getChildren().remove(fileCardsStack.getChildren().get(i));
                        size = fileCardsStack.getChildren().size();
                    }
                }
//                fileCardsStack.getChildren().stream().filter((card) -> (card.getId() == null ? false : card.getId().contains(selectCapacitorDrop.getValue()))).forEachOrdered((card) -> {
//                    fileCardsStack.getChildren().remove(card);
//                });
            }
        });
    }

    public void handleRemoveAllFilesClick() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                graphFile.getData().clear();
                fileCardsStack.getChildren().clear();
            }
        });
    }

    public void handleSelectStatsDropClick() {
        final List<String> files = new LinkedList(fileReader.getFileRawList(folderData));
        if (files.size() - 1 != selectStatsDrop.getItems().size()) {            // to exclude folder "raw"
            task = new Task<Void>() {
                @Override
                public Void call() {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            selectStatsDropItems.clear();
                            for (String file : files) {
                                if (!file.contains("raw")) // to exclude folder "raw"
                                {
                                    selectStatsDropItems.add(file.substring(0, file.indexOf("_")));
                                }
                            }
                            selectStatsDrop.getItems().addAll(selectStatsDropItems);
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

    public void handleAddStatsButtonClick() {
        if (selectStatsDrop.getValue() != null) {
            task = new Task<Void>() {
                @Override
                public Void call() {
                    String address = "data/";
                    for (String file : fileReader.getFileRawList(folderData)) {
                        if (file.contains(selectStatsDrop.getValue())) {
                            address += file;
                        }
                    }
                    XYChart.Series capacitanceStats = new XYChart.Series();
                    capacitanceStats.setName(selectStatsDrop.getValue());
                    try (Scanner scanner = new Scanner(new File(address));) {
                        while (scanner.hasNextLine() && !isCancelled()) {
                            try {
                                String line = scanner.nextLine();
                                if (!line.contains("#")) {
                                    cycle = Integer.parseInt(line.substring(0, line.indexOf(",")));
                                    measuredCapacity = Integer.parseInt(line.substring(line.lastIndexOf(",") + 1));
                                    capacitanceStats.getData().add(new XYChart.Data(cycle, measuredCapacity));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println("File not found!");
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            graphStats.getData().addAll(capacitanceStats);
                            dataCardsStack.getChildren().add(createDataCard(selectStatsDrop.getValue(), "", false));
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
            alert.setHeaderText("Capacitor not selected!");
            alert.setContentText("Please select capacitor in order to continue.");
            alert.showAndWait();
        }
    }

    public void handleClearStatsButtonClick() {

    }

    public void handleClearAllStatsButtonClick() {

    }

    public VBox createDataCard(String cID, String cTimestamp, boolean file) {
        return new DataCard(cID, cTimestamp, file, stage, graphFile, graphStats, fileCardsStack, dataCardsStack);
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

        selectStatsDropItems = FXCollections.observableArrayList();
        handleSelectStatsDropClick();

        selectSessionDropItems = FXCollections.observableArrayList();

        selectCapacitorDropItems = FXCollections.observableArrayList();
        handleSelectCapacitorDropClick();

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

        graphStats.setCreateSymbols(false);
        graphStats.setAnimated(false);
        graphStats.getXAxis().setLabel("Number of cycles");
        graphStats.getYAxis().setLabel("C [F]");
        graphStats.setLegendSide(Side.BOTTOM);

        graphCapacities.setAnimated(false);
        graphCapacities.getXAxis().setLabel("Capacitors");
        graphCapacities.getYAxis().setLabel("C [F]");
        graphCapacities.setLegendVisible(false);
        graphCapacities.setCategoryGap(13);
        graphCapacities.setBarGap(5);
        graphCapacities.getData().addAll(statsAverageCapacities, statsLastCapacities);

        ContextMenu contextMenuGraphFile = new ContextMenu();
        contextMenuGraphFile.getStyleClass().add("context-menu");

        MenuItem exportImageToClipboardGraphFileMenu = new MenuItem("Copy graph");
        exportImageToClipboardGraphFileMenu.getStyleClass().add("context-menu-item");
        exportImageToClipboardGraphFileMenu.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)); //KeyCombination.keyCombination("Ctrl+D")
        exportImageToClipboardGraphFileMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = graphFile.snapshot(new SnapshotParameters(), null);
                ClipboardContent cc = new ClipboardContent();
                cc.putImage(image);
                Clipboard.getSystemClipboard().setContent(cc);
                System.out.println("Ctrl+G pressed");
            }
        });

        MenuItem exportImageToFileGraphFileMenu = new MenuItem("Export graph");
        exportImageToFileGraphFileMenu.getStyleClass().add("context-menu-item");
        exportImageToFileGraphFileMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = graphFile.snapshot(new SnapshotParameters(), null);
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG images (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);

                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    } catch (IOException e) {
                        System.out.println("Image could not be written!");
                    }
                }
            }
        });

        contextMenuGraphFile.getItems().addAll(exportImageToClipboardGraphFileMenu, exportImageToFileGraphFileMenu);
        graphFile.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenuGraphFile.show(graphFile, event.getScreenX(), event.getScreenY());
            }
        });

        ContextMenu contextMenuGraphStats = new ContextMenu();

        MenuItem exportImageToClipboardGraphStatsMenu = new MenuItem("Copy graph");
        exportImageToClipboardGraphStatsMenu.getStyleClass().add("context-menu-item");
        exportImageToClipboardGraphStatsMenu.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)); //KeyCombination.keyCombination("Ctrl+D")
        exportImageToClipboardGraphStatsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = graphStats.snapshot(new SnapshotParameters(), null);
                ClipboardContent cc = new ClipboardContent();
                cc.putImage(image);
                Clipboard.getSystemClipboard().setContent(cc);
                System.out.println("Ctrl+G pressed");
            }
        });

        MenuItem exportImageToFileGraphStatsMenu = new MenuItem("Export graph");
        exportImageToFileGraphStatsMenu.getStyleClass().add("context-menu-item");
        exportImageToFileGraphStatsMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = graphStats.snapshot(new SnapshotParameters(), null);
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG images (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);

                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    } catch (IOException e) {
                        System.out.println("Image could not be written!");
                    }
                }
            }
        });

        contextMenuGraphStats.getItems().addAll(exportImageToClipboardGraphStatsMenu, exportImageToFileGraphStatsMenu);
        graphStats.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenuGraphStats.show(graphStats, event.getScreenX(), event.getScreenY());
            }
        });

        ContextMenu contextMenuGraphCapacities = new ContextMenu();

        MenuItem exportImageToClipboardGraphCapacitiesMenu = new MenuItem("Copy graph");
        exportImageToClipboardGraphCapacitiesMenu.getStyleClass().add("context-menu-item");
        exportImageToClipboardGraphCapacitiesMenu.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN)); //KeyCombination.keyCombination("Ctrl+D")
        exportImageToClipboardGraphCapacitiesMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = graphCapacities.snapshot(new SnapshotParameters(), null);
                ClipboardContent cc = new ClipboardContent();
                cc.putImage(image);
                Clipboard.getSystemClipboard().setContent(cc);
                System.out.println("Ctrl+G pressed");
            }
        });

        MenuItem exportImageToFileGraphCapacitiesMenu = new MenuItem("Export graph");
        exportImageToFileGraphCapacitiesMenu.getStyleClass().add("context-menu-item");
        exportImageToFileGraphCapacitiesMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = graphCapacities.snapshot(new SnapshotParameters(), null);
                FileChooser fileChooser = new FileChooser();
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PNG images (*.png)", "*.png");
                fileChooser.getExtensionFilters().add(extFilter);

                File file = fileChooser.showSaveDialog(stage);

                if (file != null) {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                    } catch (IOException e) {
                        System.out.println("Image could not be written!");
                    }
                }
            }
        });

        contextMenuGraphCapacities.getItems().addAll(exportImageToClipboardGraphCapacitiesMenu, exportImageToFileGraphCapacitiesMenu);
        graphCapacities.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenuGraphCapacities.show(graphCapacities, event.getScreenX(), event.getScreenY());
            }
        });

        dropShadow.setRadius(10);
        dropShadow.setColor(Color.color(0, 0, 0, 0.2));

    }

}
