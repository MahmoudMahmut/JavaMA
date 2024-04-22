import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class AasApiEndpoint {

    private String endpoint = "http://localhost";
    private String repoPort = "8081";
    private HttpClient client = HttpClient.newHttpClient();

    public AasApiEndpoint() {
        this("http://localhost", "8081");
    }

    public AasApiEndpoint(String endpoint, String repoPort) {
        this.endpoint = endpoint;
        this.repoPort = repoPort;
    }

    public String getBase64Str(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public void clearRepo(boolean shells, boolean submodels) throws Exception {
        // HttpClient client = HttpClient.newHttpClient();
        // try {
        if (shells) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint + ":" + repoPort + "/shells"))
                    .DELETE() // Verwendung der DELETE Methode
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Clearing shells: " + response.body());
        }
        if (submodels) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(endpoint + ":" + repoPort + "/submodels"))
                    .DELETE() // Verwendung der DELETE Methode
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Clearing submodels: " + response.body());
        }
    }
    // catch (Exception e) {
    // e.printStackTrace();
    // }

    // }

    public void uploadShell(String identifier, Map<String, Object> objectStore) throws Exception {
        if (!objectStore.containsKey(identifier)) {
            System.out.println("Shell not found in object store");
            return;
        }

        JSONObject shell = new JSONObject(objectStore.get(identifier));
        String aashellJsonString = shell.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(endpoint + ":" + repoPort + "/shells"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(aashellJsonString))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println(identifier + " " + response.body());

        // Angenommen, die Shell hat eine Methode getSubmodels, die die Identifikatoren
        // der Submodelle zurückgibt. Hier prüfen wir erst, ob der Key "submodels"
        // existiert, bevor darauf zugegriffen wird

        if (shell.has("submodels")) {
            JSONArray submodels = shell.getJSONArray("submodels");
            for (int i = 0; i < submodels.length(); i++) {
                String submodelId = submodels.getString(i);
                if (!objectStore.containsKey(submodelId))
                    continue;

                JSONObject submodel = new JSONObject(objectStore.get(submodelId));
                String smJsonString = submodel.toString();

                request = HttpRequest.newBuilder()
                        .uri(new URI(endpoint + ":" + repoPort + "/submodels"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(smJsonString))
                        .build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(submodelId + " " + response.body());
            }
        }
    }

    // Getter für endpoint
    public String getEndpoint() {
        return endpoint;
    }

    // Getter für repoPort
    public String getRepoPort() {
        return repoPort;
    }

}
