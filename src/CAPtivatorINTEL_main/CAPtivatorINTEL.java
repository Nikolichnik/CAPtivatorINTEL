package CAPtivatorINTEL_main;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CAPtivatorINTEL extends Application {

//    GUIController gui = new GUIController();
    private Stage stage = new Stage();
    private String sceneFile = "/CAPtivatorINTEL_main/FXMLDocument.fxml";
    private URL url = getClass().getResource(sceneFile);
    private Parent root;

    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(url);
            root = (Parent) loader.load(url);
            GUIController gui = new GUIController();
            System.out.println("  fxmlResource = " + sceneFile);
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

    public static void main(String[] args) {
        launch(args);
    }

}
