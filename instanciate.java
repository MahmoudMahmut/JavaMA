import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;

public class instanciate {
    private AasApiEndpoint api = new AasApiEndpoint();
    // private AasHelper helper = new AasHelper();
    private Map<String, String> bom = new HashMap<>();
    // private String baseEndpoint = "localhost";
    private HttpClient client = HttpClient.newHttpClient();

    public instanciate() {
        // Konfiguration der Bill of Materials (BOM)
        bom.put("https://example.com/ids/AssetAdministrationShell/7487_1553_5310_3896",
                "https://markt.aas-suite.de/api");
        bom.put("https://example.com/ids/AssetAdministrationShell/4506_9976_7663_8793",
                "https://markt.aas-suite.de/api");
        bom.put("https://example.com/ids/AssetAdministrationShell/2831_2382_1560_6063",
                "https://markt.aas-suite.de/api");
    }

    // Methode zum Erstellen einer Instanz

    public void createInstance(String typeShellIdentifier) throws Exception {
        // Abrufen des Type Shell
        String url = api.getEndpoint() + ':' + api.getRepoPort() + "/shells/" + api.getBase64Str(typeShellIdentifier);
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JSONObject typeShell = new JSONObject(response.body());

        // Bearbeiten des AAS zum Typ
        modifyAssetAdministrationShell(typeShell);

        // // Hochladen der Objektspeicherung
        // api.uploadObjectStore(helper.getObjectStore());
    }

    // Methode zu Modifikation und Verarbeitung der AAS
    private void modifyAssetAdministrationShell(JSONObject aas) {
        // Einstellen der Instanz
        aas.put("asset_information", new JSONObject()
                .put("asset_kind", "instance")
                .put("id", "www.hackerthon.de/ids/aas/Fahrrad_Instance_001")
                .put("global_asset_id", "www.hackerthon.de/ids/aas/Fahrrad_Instance_001"));

        aas.put("display_name", new JSONObject().put("de", "Fahrrad_001"));
        aas.put("id_short", "FahrradInstance_001");

        // Verarbeitung der Submodelle
        processSubmodels(aas);
    }

    private void processSubmodels(JSONObject aas) {
        JSONArray submodels = aas.getJSONArray("submodels");
        JSONArray newSubmodels = new JSONArray();

        for (int i = 0; i < submodels.length(); i++) {
            JSONObject originalSubmodel = submodels.getJSONObject(i);
            JSONObject clonedSubmodel = new JSONObject(originalSubmodel.toString()); // Einfache Kopie, tiefe Kopie je
                                                                                     // nach Bedarf
            String newId = originalSubmodel.getString("id") + "Instance001";

            clonedSubmodel.put("id", newId);
            updateSubmodel(clonedSubmodel); // Methode implementieren, um weitere Anpassungen vorzunehmen
            newSubmodels.put(clonedSubmodel);
        }
        aas.put("submodels", newSubmodels); // Ersetze alte Submodelle durch neue
    }

    private void updateSubmodel(JSONObject submodel) {
        // Anpassen des Submodells, zum Beispiel:
        // submodel.put("einigeFelder", "neueWerte");
    }

    // da es in AssHelper keine Methode "getElementByIdPath" gibt!!!!

    // private void setSerialNumberAndOtherDetails(JSONObject aas) {
    // // Seriennummer und andere Details setzen
    // JSONObject nameplate = helper.getElementByIdPath(aas,
    // "Nameplate.URIOfTheProduct");
    // nameplate.put("value", nameplate.getString("value") + "_Instance_001");
    // helper.getElementByIdPath(aas, "Nameplate.YearOfConstruction").put("value",
    // "2024");
    // helper.getElementByIdPath(aas, "Nameplate.DateOfManufacture").put("value",
    // "Mrz");
    // helper.getElementByIdPath(aas,
    // "Nameplate.WarrantyUntil").put("value","2026");
    // }

    private void setSerialNumberAndOtherDetails(JSONObject aas) {
        // Direkter Zugriff auf die JSON-Objekte anhand des Pfades
        JSONObject nameplate = aas.getJSONObject("Nameplate").getJSONObject("URIOfTheProduct");
        nameplate.put("value", nameplate.getString("value") + "_Instance_001");

        aas.getJSONObject("Nameplate").getJSONObject("YearOfConstruction").put("value", "2024");
        aas.getJSONObject("Nameplate").getJSONObject("DateOfManufacture").put("value", "Mrz");
        aas.getJSONObject("Nameplate").getJSONObject("WarrantyUntil").put("value", "2026");
    }

    public void uploadObjectStore(Map<String, JSONObject> objectStore) {
        // Implementiere das Hochladen der Objektspeicherung zur API
        // Dies kann HTTP-Requests beinhalten, die die JSON-Objekte senden

    }

    public static void main(String[] args) {
        try {
            instanciate creator = new instanciate();
            creator.createInstance("www.hackerthon.de/ids/aas/TypeFahrrad");
        } catch (Exception e) {
            System.out.println("Fehler bei der Erstellung der Instanz: " + e.getMessage());
        }
    }
}
