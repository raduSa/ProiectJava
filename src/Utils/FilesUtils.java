package Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilesUtils {

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
