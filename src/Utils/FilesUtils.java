package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilesUtils {
    
    /**
     * Reads properties from a file and returns them as a list of strings
     * @param fileName The name of the file to read from
     * @return List of strings, one for each line in the file
     * @throws IOException If an I/O error occurs
     */
    public static List<String> citireProprietati(String fileName) throws IOException {
        List<String> properties = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                properties.add(line);
            }
        }
        
        return properties;
    }
}
