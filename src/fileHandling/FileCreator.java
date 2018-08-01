package fileHandling;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class FileCreator {

    private String input; // za prenos info poput filename, destination i slično

    public PrintWriter createPrinterWriter(String fileName) {
        FileWriter fw = null;
        Writer bw = null;
        try {
            fw = new FileWriter(fileName, true);
            bw = new BufferedWriter(fw);
        } catch (IOException ex) {
            System.out.println("Unable to create file!");
        }
        return new PrintWriter(bw);
    }

}
