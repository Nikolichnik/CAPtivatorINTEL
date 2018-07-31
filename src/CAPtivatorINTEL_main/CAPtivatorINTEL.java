package CAPtivatorINTEL_main;

import java.net.URL;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
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
            GUIController gui = new GUIController();
            gui.setStage(stage);
            Scene scene = new Scene(root, 950, 530, Color.TRANSPARENT);
            gui.setScene(scene);
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
