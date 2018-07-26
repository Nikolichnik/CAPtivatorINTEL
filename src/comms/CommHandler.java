package comms;

import com.fazecast.jSerialComm.SerialPort;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

public class CommHandler implements Initializable {

    private SerialPort chosenPort;
    private String input;
    private SerialPort[] portNames;
    private final ObservableList<String> portList = FXCollections.observableArrayList();

    public SerialPort getChosenPort() {
        return chosenPort;
    }

    public void setChosenPort(SerialPort chosenPort) {
        this.chosenPort = chosenPort;
    }

    public String getInput() {        
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }   

    public ObservableList<String> getPortList() {
        portNames = SerialPort.getCommPorts();
        portList.clear();
        for (SerialPort portName : this.portNames) {
            portList.add(portName.getSystemPortName());
        }
        return portList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


}
