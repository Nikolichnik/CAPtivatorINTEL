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

    private Stage stage = new Stage();

    private String sceneFile = "/CAPtivatorINTEL_main/FXMLDocument.fxml";
    private URL url = getClass().getResource(sceneFile);
    private HBox root;

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
    ObservableList<String> dropItems = comms.getPortList();
    private ComboBox<String> selectPortDrop = new ComboBox();

    @FXML
    private ToggleButton connectButton;    

    public void loadGUI() {
        try {
            root = (HBox) FXMLLoader.load(url);
            System.out.println("  fxmlResource = " + sceneFile);
            Scene scene = new Scene((Parent) root, 900, 500);
            stage.setTitle("CAPtivatorINTEL");
            selectPortDrop.setTooltip(new Tooltip());
            selectPortDrop.getItems().addAll(dropItems);           
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.print("  * ");
            ex.printStackTrace();
            System.out.println("    ----------------------------------------\n");
        }
        
        System.out.println(dropItems);
        
        
    }
    
    @FXML
    void handleButtonAction(ActionEvent event) {

    }
    
    public void handleConnectClick() {

    }

    public void handleFileReadClick() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        selectPortDrop
    }

}
