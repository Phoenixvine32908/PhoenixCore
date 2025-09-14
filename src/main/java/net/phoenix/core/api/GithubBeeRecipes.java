package net.phoenix.core.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GithubBeeRecipes {

    public static void main(String[] args) {
        // Define the GitHub API URL for the specified directory and branch.
        String gitHubApiUrl = "https://api.github.com/repos/JDKDigital/productive-bees/contents/src/main/resources/data/productivebees/recipe/bee_conversion?ref=dev-1.21.0";

        // --- This is the new, automated part ---
        List<String> beeNames;
        try {
            beeNames = getBeeNamesFromGitHub(gitHubApiUrl);
            if (beeNames.isEmpty()) {
                System.out.println("No bee breeding recipe files found at the GitHub URL.");
                return;
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to get bee names from GitHub. Using a fallback list.");
            e.printStackTrace();
            // Fallback to a hardcoded list if the API call fails.
            beeNames = List.of("honey_bee", "nether_bee", "rocky_bee");
        }

        // --- The rest of the code is the same as before ---

        // Define the base directory where the new JSON files will be created.
        // IMPORTANT: Change this to the correct path for your project.
        String baseDirectory = "C:\\Users\\conno\\curseforge\\minecraft\\Instances\\Phoenix Forge Technologies\\kubejs\\assets\\productivebees\\recipes\\bee_breeding\\";

        // Define the template JSON content.
        String jsonContent = "{}";

        // ... (The rest of the file writing logic from the previous example) ...
        // You would place the file writing loop here, using the 'beeNames' list.

        System.out.println("Successfully retrieved " + beeNames.size() + " bee names from GitHub:");
        System.out.println(beeNames);
    }

    /**
     * Connects to the GitHub API to retrieve a list of filenames from a given directory.
     * 
     * @param apiUrl The GitHub API URL for the directory.
     * @return A list of bee names (filenames without the .json extension).
     * @throws IOException          If there is a problem with the HTTP request.
     * @throws InterruptedException If the request is interrupted.
     */
    private static List<String> getBeeNamesFromGitHub(String apiUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json") // Good practice
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get data from GitHub API. Status code: " + response.statusCode());
        }

        List<String> beeNames = new ArrayList<>();
        // Simple regex to extract filenames from the JSON response
        Pattern pattern = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response.body());

        while (matcher.find()) {
            String fileName = matcher.group(1);
            if (fileName.endsWith(".json")) {
                // Remove the .json extension to get the clean name
                String beeName = fileName.replace(".json", "");
                beeNames.add(beeName);
            }
        }

        return beeNames;
    }
}
