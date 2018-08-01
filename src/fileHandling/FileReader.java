package fileHandling;

import java.io.File;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FileReader {

    private String output;

    ObservableList<String> selectFileDropItems = FXCollections.observableArrayList(); ;

    public ObservableList<String> getFileRawList(File folderRaw) {
        
        selectFileDropItems.clear();
        File[] listOfFiles = folderRaw.listFiles();

        for (File file : listOfFiles) {
            if (file != null) {
                selectFileDropItems.add(file.getName());
            }
        }
        return selectFileDropItems;
    }

}
