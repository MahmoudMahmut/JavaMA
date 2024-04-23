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

        // Zuerst das Repository bereinigen
        api.clearRepo(true, true); // Löscht sowohl Shells als auch Submodels

        // Type Shell herunterlade
        String url = api.getEndpoint() + ':' + api.getRepoPort() + "/shells/" + api.getBase64Str(typeShellIdentifier);
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("API-Antwort: " + response.body()); // Zum Debuggen
        JSONObject typeShell = new JSONObject(response.body());

        // // Bearbeiten des AAS zum Typ
        // modifyAssetAdministrationShell(typeShell);

        // // Hochladen der Objektspeicherung
        // uploadObjectStore(typeShell);

        if (typeShell.has("id")) {
            modifyAssetAdministrationShell(typeShell);
            uploadObjectStore(typeShell); // AAS hochladen
            uploadSubmodels(typeShell); // Submodelle hochladen

        } else {
            System.out.println("Fehler: Die AAS-ID ist im JSON-Objekt nicht vorhanden.");
        }

    }

    // Methode zu Modifikation und Verarbeitung der AAS
    private void modifyAssetAdministrationShell(JSONObject aas) {

        JSONObject assetInfo = aas.optJSONObject("assetInformation");
        if (assetInfo == null) {
            System.out.println("Fehler: 'asset_information' fehlt im JSON-Objekt.");
            return;
        }

        // Setzen der Instanzinformationen

        assetInfo.put("asset_kind", "instance");
        assetInfo.put("global_asset_id", aas.getString("id") + "_Instance");

        aas.put("display_name", new JSONObject().put("de", "Fahrrad_001"));
        aas.put("id_short", "FahrradInstance_001");

        processSubmodels(aas);
    }

    private void processSubmodels(JSONObject aas) {
        JSONArray submodels = aas.optJSONArray("submodels");

        if (submodels == null) {
            System.out.println("Keine Submodelle gefunden.");
            return;
        }

        JSONArray newSubmodels = new JSONArray();

        for (int i = 0; i < submodels.length(); i++) {
            JSONObject submodel = submodels.getJSONObject(i);
            JSONObject newSubmodel = new JSONObject(submodel.toString()); // Tiefe Kopie

            // String newId = submodel.optString("id") + "Instance001";
            String newId = "www.hackerthon.de/ids/aas/Fahrrad_" + submodel.optString("id") + "Instance_001"; // Korrekte
                                                                                                             // ID
                                                                                                             // generieren
            newSubmodel.put("id", newId);
            updateSubmodelAttributes(newSubmodel);
            newSubmodels.put(newSubmodel);
        }
        aas.put("submodels", newSubmodels); // Ersetze alte Submodelle durch neue
    }

    private void updateSubmodelAttributes(JSONObject submodel) {
        submodel.put("status", "aktualisiert");
        // Beispiel für weitere notwendige Anpassungen
        // submodel.put("neuesAttribut", "neuerWert");
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

    public void uploadObjectStore(JSONObject aas) {
        try {
            String uploadUrl = api.getEndpoint() + ':' + api.getRepoPort() + "/shells";
            HttpRequest uploadRequest = HttpRequest.newBuilder()
                    .uri(new URI(uploadUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(aas.toString()))
                    .build();
            HttpResponse<String> uploadResponse = client.send(uploadRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Upload response for AAS: " + uploadResponse.statusCode() + " - " + uploadResponse.body());
        } catch (Exception e) {
            System.out.println("Fehler beim Hochladen des AAS: " + e.getMessage());
        }
    }

    private void uploadSubmodels(JSONObject aas) {
        JSONArray submodels = aas.getJSONArray("submodels");

        for (int i = 0; i < submodels.length(); i++) {
            JSONObject submodel = submodels.getJSONObject(i);
            try {
                // String submodelId = submodel.optString("id") + "Instance001";
                String uploadUrl = api.getEndpoint() + ':' + api.getRepoPort() + "/submodels";
                HttpRequest uploadRequest = HttpRequest.newBuilder()
                        .uri(new URI(uploadUrl))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(submodel.toString()))
                        .build();
                HttpResponse<String> uploadResponse = client.send(uploadRequest,
                        HttpResponse.BodyHandlers.ofString());
                // System.out.println("Upload response for submodel " + submodelId + ": " +
                // uploadResponse.statusCode()
                // + " - " + uploadResponse.body());
                System.out.println("Upload response for submodel " + submodel.getString("id") + ": "
                        + uploadResponse.statusCode() + " - " + uploadResponse.body());
            } catch (Exception e) {
                System.out.println("Fehler beim Hochladen des Submodells: " + e.getMessage());
            }
        }

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
