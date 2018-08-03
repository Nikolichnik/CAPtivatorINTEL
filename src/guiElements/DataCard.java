package guiElements;

import fileHandling.FileReader;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Nikolichnik
 */
public final class DataCard extends VBox {

    private int cNominal = 0, cInitial = 0, cLast = 0, cMeasured = 0, vBoxWidth = 90, titleHeight = 29;
    private int Cmin = 0, Cmiddle = 0, Cmax = 0, Imin = 0, Imiddle = 0, Imax = 0;
    private int Qmin = 0, Qmiddle = 0, Qmax = 0, Emin = 0, Emiddle = 0, Emax = 0;
    private long days = 0;
    private double nominalVoltage = 2.7, nominalVoltageLocal = 2.7;

    private final String cID, cTimestamp;

    private boolean file;

    private final Stage stage;

    private DropShadow dropShadow = new DropShadow();

    private final FileReader fileReader = new FileReader();
    private final File folderRaw = new File("data/raw/");
    private final File folderData = new File("data/");
    
    private LineChart<?, ?> graphFile, graphStats;
    
    private HBox fileCardsStack, dataCardsStack;

    public DataCard(String cIDIn, String cTimestampIn, boolean fileIn, Stage stageIn, LineChart<?, ?> graphFileIn, LineChart<?, ?> graphStatsIn, HBox fileCardsStackIn, HBox dataCardsStackIn) {
        this.cID = cIDIn;
        this.cTimestamp = cTimestampIn;
        this.file = fileIn;
        this.stage = stageIn;
        this.graphFile = graphFileIn;
        this.graphStats = graphStatsIn;
        this.fileCardsStack = fileCardsStackIn;
        this.dataCardsStack = dataCardsStackIn;
        

        dropShadow.setRadius(10);
        dropShadow.setColor(Color.color(0, 0, 0, 0.2));

        String addressData = "data/" + cID + "_all.txt";
        String addressRaw = "data/raw/";

        String dateStart = "N/A", dateLast = "N/A";

        DateTimeFormatter dtfIn = DateTimeFormatter.ofPattern("uuuuMMddHHmm");
        DateTimeFormatter dtfTimestamp = DateTimeFormatter.ofPattern("MMddHHmm");
        DateTimeFormatter dtfOut = DateTimeFormatter.ofPattern("dd/MM");

        for (String fileRaw : fileReader.getFileRawList(folderRaw)) {
            if (fileRaw.contains(cID) && fileRaw.contains(cTimestamp)) {
                addressRaw += fileRaw;
            }
        }

        List<Integer> capacitances = new LinkedList();
        List<String> dates = new LinkedList();
        try (Scanner scanner = new Scanner(new File(addressData));) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("#")) {
                    cNominal = Integer.parseInt(line.substring(line.lastIndexOf(",") + 1, line.length()));
                    nominalVoltageLocal = Double.parseDouble(line.substring(line.indexOf(",") + 1, line.lastIndexOf(",")));
                } else {
                    capacitances.add(Integer.parseInt(line.substring(line.lastIndexOf(",") + 1, line.length())));
                    dates.add(Collections.list(new StringTokenizer(line, ",", false)).stream().map(token -> (String) token).collect(Collectors.toList()).get(1));
                }
                if (line.contains(cTimestamp)) {
                    cMeasured = Integer.parseInt(line.substring(line.lastIndexOf(",") + 1, line.length()));
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("_all file not found!!");
        }

        if (capacitances.size() != 0) {
            cInitial = capacitances.get(0);
            cLast = capacitances.get(capacitances.size() - 1);
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
        }

        if (dates.size() != 0) {
            LocalDate dateStartRaw = LocalDate.parse(dates.get(0), dtfIn);
            dateStart = dtfOut.format(dateStartRaw);
            LocalDate dateLastRaw = LocalDate.parse(dates.get(dates.size() - 1), dtfIn);
            dateLast = dtfOut.format(dateLastRaw);
            days = ChronoUnit.DAYS.between(dateStartRaw, dateLastRaw);
        }

        List<Integer> currents = new LinkedList();
        if (file) {
            try (Scanner scanner = new Scanner(new File(addressRaw));) {
                while (scanner.hasNextLine()) {
                    currents.add(Collections.list(new StringTokenizer(scanner.nextLine(), ",", false)).stream().map(token -> Integer.parseInt((String) token)).collect(Collectors.toList()).get(1));
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
            Qmin = (int) (Cmin * nominalVoltage);
            Qmiddle = (int) (Cmiddle * nominalVoltage);
            Qmax = (int) (Cmax * nominalVoltage);

            Emin = (int) (0.5 * Qmin * nominalVoltage);
            Emiddle = (int) (0.5 * Qmiddle * nominalVoltage);
            Emax = (int) (0.5 * Qmax * nominalVoltage);
        }

        VBox dataCard = this;
        dataCard.setId(cID);
        dataCard.setMinWidth(vBoxWidth);
        dataCard.setMaxWidth(vBoxWidth);
        dataCard.setMinHeight(300);
        dataCard.setStyle("-fx-background-color: #F5F5F5;");
        dataCard.setAlignment(Pos.CENTER);
        dataCard.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("context-menu");

        MenuItem exportImageToClipboardDataCardMenu = new MenuItem("Copy graph with data");
        exportImageToClipboardDataCardMenu.getStyleClass().add("context-menu-item");
        exportImageToClipboardDataCardMenu.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN)); //KeyCombination.keyCombination("Ctrl+D")
        exportImageToClipboardDataCardMenu.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = dataCard.getParent().getParent().snapshot(new SnapshotParameters(), null);
                ClipboardContent cc = new ClipboardContent();
                cc.putImage(image);
                Clipboard.getSystemClipboard().setContent(cc);
                System.out.println("Ctrl+D pressed");
            }
        });

        MenuItem exportImageToFileDataCardMenu = new MenuItem("Export graph with data");
        exportImageToFileDataCardMenu.getStyleClass().add("context-menu-item");
        exportImageToFileDataCardMenu.setAccelerator(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN));
        exportImageToFileDataCardMenu.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                WritableImage image = dataCard.getParent().getParent().snapshot(new SnapshotParameters(), null);
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

        MenuItem goToFileDataCardMenu = new MenuItem("Open source file");
        goToFileDataCardMenu.getStyleClass().add("context-menu-item");
//        goToFileDataCardMenu.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
        final String addressOpenRawFile = addressRaw;
        goToFileDataCardMenu.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    if (file) {
                        Desktop.getDesktop().open(new File(addressOpenRawFile));
                    } else {
                        Desktop.getDesktop().open(new File(addressData));
                    }
                } catch (IOException ex) {
                    System.out.println("File could not be located!");
                }
            }
        });

        MenuItem removeCapacitorMenu = new MenuItem("Remove capacitor " + cID);
        removeCapacitorMenu.getStyleClass().add("context-menu-item");
        removeCapacitorMenu.setOnAction(new javafx.event.EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (dataCard.getParent().getId().equals("fileCardsStack")) {
                    String cTimestampID = cTimestamp.substring(2, 4) + "/" + cTimestamp.substring(0, 2) + " | " + cTimestamp.substring(4, 6) + ":" + cTimestamp.substring(6, 8);
                    graphFile.getData().removeIf((XYChart.Series data) -> data.getName().contains(cTimestampID));
                    fileCardsStack.getChildren().remove(dataCard);
                } else {
                    int size = graphStats.getData().size();
                    for (int i = 0; i < size; i++) {
                        if (graphStats.getData().get(i).getName().contains(cID)) {
                            graphStats.getData().remove(i);
                            size = graphStats.getData().size();
                        }
                    }
                    dataCardsStack.getChildren().remove(dataCard);
                }
            }
        });

        contextMenu.getItems().addAll(exportImageToClipboardDataCardMenu, exportImageToFileDataCardMenu, goToFileDataCardMenu, new SeparatorMenuItem(), removeCapacitorMenu);
        dataCard.setOnContextMenuRequested(new javafx.event.EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(dataCard, event.getScreenX(), event.getScreenY());
            }
        });

        Label title = new Label(cID);
        if (file) {
            title.setText(cID + " | " + cTimestamp.substring(0, 2) + "/" + cTimestamp.substring(2, 4));
        }
        title.setMinSize(vBoxWidth, titleHeight);
        title.setMaxSize(vBoxWidth, titleHeight);
        title.setAlignment(Pos.CENTER);
        title.setPadding(new Insets(9, 0, 5, 0));

        VBox nominal = createDatesCell("Nominal", cID, cNominal, String.valueOf(nominalVoltageLocal) + "V");
        VBox cellCapacitance = createDataCell("C [F]", Cmin, Cmiddle, Cmax);
        VBox cellQ = createDataCell("Q [C]", Qmin, Qmiddle, Qmax);
        VBox cellEnergy = createDataCell("E [J]", Emin, Emiddle, Emax);

        Pane bumper = new Pane();
        bumper.setMinSize(30, 9);
        bumper.setMaxSize(30, 9);

        dataCard.getChildren().addAll(title, makeDoughnut(Cmiddle, cNominal, false), makeDoughnut(Cmiddle, cInitial, true), bumper, nominal, cellCapacitance);  //doughnutNominal, doughnutInitial,

        if (file) {
            VBox cellCurrent = createDataCell("I [mA]", Imin, Imiddle, Imax);
            dataCard.getChildren().addAll(cellCurrent);
        }

        VBox cellDates = createDatesCell("Dates", dateStart, days, dateLast);
        
        dataCard.getChildren().addAll(cellQ, cellEnergy, cellDates);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        dataCard.getChildren().add(spacer);
        dataCard.setEffect(dropShadow);
    }

    public VBox createDataCell(String title, int leftValue, int middleValue, int rightValue) {
        VBox dataCell = new VBox();
        dataCell.setMaxSize(90, 45);
        dataCell.setAlignment(Pos.CENTER);

        VBox titleVBox = new VBox();
        Label cellTitle = new Label(title);
        cellTitle.setAlignment(Pos.CENTER);
        cellTitle.setMaxHeight(19);
        cellTitle.setMinHeight(19);
        titleVBox.setAlignment(Pos.CENTER);
        titleVBox.getChildren().add(cellTitle);
        titleVBox.setStyle("-fx-background-color: #F5F5F5;");
        titleVBox.setEffect(dropShadow);

        HBox values = new HBox();
        values.setAlignment(Pos.CENTER);

        String leftString = String.valueOf(leftValue);
        String middleString = String.valueOf(middleValue);
        String rightString = String.valueOf(rightValue);

        Label leftPad = new Label();
        leftPad.setMinWidth(1);
        leftPad.setMaxWidth(1);

        Label left = new Label(leftString);
        left.setFont(new Font(9.0));
        left.setMinWidth(26);
        left.setMaxWidth(26);
        left.setAlignment(Pos.CENTER);

        Label middle = new Label(middleString);
        middle.setPrefWidth(33);
        middle.setAlignment(Pos.CENTER);

        Label right = new Label(rightString);
        right.setFont(new Font(9.0));
        right.setMinWidth(26);
        right.setMaxWidth(26);
        right.setAlignment(Pos.CENTER);

        Label rightPad = new Label();
        rightPad.setMinWidth(1);
        rightPad.setMaxWidth(1);

        values.setMaxHeight(23);
        values.setMinHeight(23);
        values.getChildren().addAll(leftPad, left, middle, right, rightPad);
        dataCell.getChildren().addAll(titleVBox, values);

        return dataCell;
    }

    public VBox createDatesCell(String title, String dateStart, long days, String dateLast) {
        VBox datesCell = new VBox();
        datesCell.setMaxSize(90, 45);
        datesCell.setAlignment(Pos.CENTER);

        VBox titleVBox = new VBox();
        Label cellTitle = new Label(title);
        cellTitle.setAlignment(Pos.CENTER);
        cellTitle.setMaxHeight(19);
        cellTitle.setMinHeight(19);
        titleVBox.setAlignment(Pos.CENTER);
        titleVBox.getChildren().add(cellTitle);
        titleVBox.setStyle("-fx-background-color: #F5F5F5;");
        titleVBox.setEffect(dropShadow);

        HBox values = new HBox();
        values.setAlignment(Pos.CENTER);

        Label leftPad = new Label();
        leftPad.setMinWidth(1);
        leftPad.setMaxWidth(1);

        Label left = new Label(dateStart);
        left.setFont(new Font(9.0));
        left.setMinWidth(26);
        left.setMaxWidth(26);
        left.setAlignment(Pos.CENTER_LEFT);

        Label middle = new Label(String.valueOf(days));
        middle.setMinWidth(26);
        middle.setAlignment(Pos.CENTER);

        Label right = new Label(dateLast);
        right.setFont(new Font(9.0));
        right.setMinWidth(26);
        right.setMaxWidth(26);
        right.setAlignment(Pos.CENTER_RIGHT);

        Label rightPad = new Label();
        rightPad.setMinWidth(1);
        rightPad.setMaxWidth(1);

        values.setMaxHeight(23);
        values.setMinHeight(23);
        values.getChildren().addAll(leftPad, left, middle, right, rightPad);
        datesCell.getChildren().addAll(titleVBox, values);

        return datesCell;
    }

    public StackPane makeDoughnut(int value, int total, boolean percentOption) {
        StackPane doughnut = new StackPane();

        int percent = 0;
        Text text;

        if (percentOption) {
            if (total != 0) {
                percent = value * 100 / total;
                text = new Text(String.valueOf(percent) + "%");
            } else {
                text = new Text("N/A");
            }
        } else {
            if (value != 0) {
                text = new Text(String.valueOf(value));
            } else {
                text = new Text("N/A");
            }
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(new PieChart.Data("", value), new PieChart.Data("", total - value));
        PieChart pie = new PieChart(pieData);
        pie.setLabelsVisible(false);
        pie.setStartAngle(90);
        pie.setEffect(dropShadow);

        text.setFont(new Font(23));
        text.setEffect(dropShadow);

        Circle innerCircle = new Circle();
        innerCircle.setRadius(33);
        innerCircle.setFill(Color.SNOW);  // do in CSS!
        innerCircle.setStroke(Color.WHITE);
        innerCircle.setStrokeWidth(2);

        doughnut.getChildren().addAll(pie, innerCircle, text);
        doughnut.setAlignment(Pos.CENTER);
        doughnut.setMaxSize(30, 30);
        doughnut.setMinSize(90, 90);
        doughnut.setScaleX(0.65);
        doughnut.setScaleY(0.65);

        return doughnut;
    }

}
