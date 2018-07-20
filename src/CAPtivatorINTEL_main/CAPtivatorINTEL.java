package CAPtivatorINTEL_main;

import javafx.application.Application;
import javafx.stage.Stage;

public class CAPtivatorINTEL extends Application {

    GUIController gui = new GUIController();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {      

        gui.loadGUI();
        
    }

}
