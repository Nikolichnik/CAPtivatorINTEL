package comms;

import CAPtivatorINTEL_main.GUIController;
import com.fazecast.jSerialComm.SerialPort;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;

public class CommHandler implements Initializable {

    private SerialPort chosenPort;
    private String input;
    private SerialPort[] portNames = SerialPort.getCommPorts();
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

    public SerialPort[] getPortNames() {
        return portNames;
    }

    public void setPortNames(SerialPort[] portNames) {
        this.portNames = portNames;
    }

    public ObservableList<String> getPortList() {
        for (SerialPort portName : this.portNames) {
            portList.add(portName.getSystemPortName());
        }
        return portList;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

}
