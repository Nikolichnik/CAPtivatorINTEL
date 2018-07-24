package CAPtivatorINTEL_main;

import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CAPtivatorINTEL extends Application {

    private final URL url = getClass().getResource("/CAPtivatorINTEL_main/FXMLDocument.fxml");
    private Parent root;

    @Override
    public void start(Stage stage) {
        try {
            root = (Parent) FXMLLoader.load(url);
//            System.out.println("  fxmlResource = " + sceneFile);
            Scene scene = new Scene(root, 900, 500);
            stage.setTitle("CAPtivatorINTEL");
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.print("  * ");
            System.out.println("    ----------------------------------------\n");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
