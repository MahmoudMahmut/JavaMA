
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.*;
import org.json.JSONObject;

import java.io.FileReader;
import com.opencsv.CSVReader;

public class AasToolchain {

    private AasApiEndpoint api = new AasApiEndpoint();
    private AasHelper newAasHelper = new AasHelper();

    // private Map<String, Object> objectStore = new HashMap<>();

    public void processFiles(List<String> fileNames) throws Exception {
        for (String fileName : fileNames) {
            // CSV-Datei einlesen und verarbeiten
            List<String[]> csvData = readCsv(fileName + ".csv");
            List<String[]> nameplateData = new ArrayList<>();
            Map<String, Tuple<String, String>> submodelIdentifiers = new HashMap<>();

            for (String[] row : csvData) {
                if ("Sm".equals(row[1]) || "SmRef".equals(row[1])) {
                    submodelIdentifiers.put(row[0], new Tuple<>(row[1], row[2]));
                } else {
                    nameplateData.add(row);
                }
            }

            // JSON-Datei einlesen
            String jsonContent = new String(Files.readAllBytes(Paths.get(fileName + ".json")));
            JSONObject aasInfo = new JSONObject(jsonContent);

            // // AAS und AASX-Datei erstellen (simuliert)
            // createAasFromIdShortList(aasInfo, submodelIdentifiers, nameplateData);
            // writeAasToFile(aasInfo.getString("id"), fileName + ".aasx");

            // api.uploadShell(aasInfo.getString("id"), objectStore);
        }
    }

    // Das Lesen der CSV-Datei mit "opencsv"
    private List<String[]> readCsv(String filePath) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            return reader.readAll();
        }
    }

    // // Simuliere das Erstellen von AAS aus ID-Short-List
    // private void createAasFromIdShortList(JSONObject aasInfo, Map<String,
    // Tuple<String, String>> submodelIdentifiers,
    // List<String[]> nameplateData) {
    // // Implementierung abhängig von der Anforderungen
    // }

    // // Simuliere das Schreiben der AASX-Datei
    // private void writeAasToFile(String aasId, String filePath) throws Exception {
    // try (FileWriter writer = new FileWriter(filePath)) {
    // writer.write("AASX content for " + aasId); // Simulierter Inhalt
    // }
    // }

    // Hilfsklasse für Tuples
    private class Tuple<X, Y> {
        public final X x;
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) throws Exception {
        AasToolchain toolchain = new AasToolchain();
        // List<String> fileNames = List.of("typeFahrrad"); // Beispiel-Dateiname
        // toolchain.processFiles(fileNames);

    }
}
