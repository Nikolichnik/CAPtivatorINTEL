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

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage stage) {
        try {
            root = (Parent) FXMLLoader.load(url);
//            System.out.println("  fxmlResource = " + sceneFile);
            root.getStylesheets().add(getClass().getResource("/CAPtivatorINTEL_main/style.css").toExternalForm());
            Scene scene = new Scene(root, 900, 500, Color.TRANSPARENT);
            stage.setTitle("CAPtivatorINTEL");
            stage.initStyle(TRANSPARENT);
//            root.setOnMousePressed(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    xOffset = event.getSceneX();
//                    yOffset = event.getSceneY();
//                }
//            });
//            root.setOnMouseDragged(new EventHandler<MouseEvent>() {
//                @Override
//                public void handle(MouseEvent event) {
//                    stage.setX(event.getScreenX() - xOffset);
//                    stage.setY(event.getScreenY() - yOffset);
//                }
//            });
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
    
    private javafx.scene.shape.Rectangle makeSmoke(Stage stage) {
        return new javafx.scene.shape.Rectangle(
                stage.getWidth(),
                stage.getHeight(),
                Color.WHITESMOKE.deriveColor(
                        0, 1, 1, 0.08
                )
        );
    }

    public static void main(String[] args) {
        launch(args);
    }

}
