package CAPtivatorINTEL_main;

import java.net.URL;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import static javafx.stage.StageStyle.TRANSPARENT;

public class CAPtivatorINTEL extends Application {

    private final URL url = getClass().getResource("/CAPtivatorINTEL_main/FXMLDocument.fxml");
    private Parent root;

    @Override
    public void start(Stage stage) {
        try {
            root = (Parent) FXMLLoader.load(url);
//            System.out.println("  fxmlResource = " + sceneFile);
            root.getStylesheets().add(getClass().getResource("/CAPtivatorINTEL_main/style.css").toExternalForm());
            Scene scene = new Scene(root, 900, 500, Color.TRANSPARENT);
            stage.setTitle("CAPtivatorINTEL");
            stage.initStyle(TRANSPARENT);
            stage.setScene(scene);
            stage.show();
        } catch (Exception ex) {
            System.out.println("Exception on FXMLLoader.load()");
            System.out.println("  * url: " + url);
            System.out.print("  * ");
            System.out.println("    ----------------------------------------\n");
            ex.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }

}
