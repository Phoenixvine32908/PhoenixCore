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
        String gitHubApiUrl = "https://api.github.com/repos/JDKDigital/productive-bees/contents/src/main/resources/data/productivebees/recipe/bee_conversion?ref=dev-1.21.0";

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
            beeNames = List.of("honey_bee", "nether_bee", "rocky_bee");
        }


        String baseDirectory = "C:\\Users\\conno\\curseforge\\minecraft\\Instances\\Phoenix Forge Technologies\\kubejs\\assets\\productivebees\\recipes\\bee_breeding\\";


        String jsonContent = "{}";


        System.out.println("Successfully retrieved " + beeNames.size() + " bee names from GitHub:");
        System.out.println(beeNames);
    }

    private static List<String> getBeeNamesFromGitHub(String apiUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to get data from GitHub API. Status code: " + response.statusCode());
        }

        List<String> beeNames = new ArrayList<>();
        Pattern pattern = Pattern.compile("\"name\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(response.body());

        while (matcher.find()) {
            String fileName = matcher.group(1);
            if (fileName.endsWith(".json")) {
                String beeName = fileName.replace(".json", "");
                beeNames.add(beeName);
            }
        }

        return beeNames;
    }
}
