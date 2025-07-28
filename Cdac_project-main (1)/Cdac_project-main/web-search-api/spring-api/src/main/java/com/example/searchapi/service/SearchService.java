package com.example.searchapi.service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.searchapi.model.CachedImageQuery;
import com.example.searchapi.model.CachedQuery;
import com.example.searchapi.model.ImageSearchResult;
import com.example.searchapi.model.SearchResult;
import com.example.searchapi.repository.CachedImageQueryRepository;
import com.example.searchapi.repository.CachedQueryRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SearchService {

    private final CachedQueryRepository repo;
    private final CachedImageQueryRepository imageRepo;

    @Value("${serpapi.api.key}")
    private String apiKey;

    private static final String SEARCH_URL = "https://serpapi.com/search";
    private static final String IMAGE_SEARCH_URL = "https://serpapi.com/search.json?engine=google_images_light";

    public SearchService(CachedQueryRepository repo, CachedImageQueryRepository imageRepo) {
        this.repo = repo;
        this.imageRepo = imageRepo;
    }

    public List<ImageSearchResult> searchImages(String query) {
        var cached = imageRepo.findByQuery(query);
        if (cached.isPresent()) {
            System.out.println("IMAGE CACHE HIT for: " + query);
            return cached.get().getResults();
        }

        System.out.println("IMAGE CACHE MISS — Calling SerpAPI for images: " + query);
        List<ImageSearchResult> results = new ArrayList<>();

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = IMAGE_SEARCH_URL + "&q=" + encoded + "&api_key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = new ObjectMapper().readTree(response.body());

            JsonNode images = json.get("images_results");
            if (images != null) {
                for (JsonNode item : images) {
                    ImageSearchResult result = new ImageSearchResult();
                    result.setTitle(item.has("title") ? item.get("title").asText() : "");
                    result.setImageUrl(item.has("thumbnail") ? item.get("thumbnail").asText() : null);
                    result.setSource(item.has("source") ? item.get("source").asText() : null);
                    results.add(result);
                }
            }

            CachedImageQuery cache = new CachedImageQuery();
            cache.setQuery(query);
            cache.setResults(results);
            cache.setCreatedAt(new Date()); // ✅ Correct usage
            imageRepo.save(cache);

            System.out.println("✅ Cached image results for: " + query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }
    
    public List<SearchResult> search(String query) {
        var cachedQuery = repo.findByQuery(query);
        if (cachedQuery.isPresent()) {
            System.out.println("CACHE HIT for query: " + query);
            return cachedQuery.get().getResults();
        }

        System.out.println("CACHE MISS — Calling SerpAPI for query: " + query);
        List<SearchResult> searchResults = new ArrayList<>();

        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = SEARCH_URL + "?q=" + encoded + "&engine=google&api_key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode json = new ObjectMapper().readTree(response.body());

            JsonNode organicResults = json.get("organic_results");
            if (organicResults != null) {
                for (JsonNode item : organicResults) {
                    SearchResult result = new SearchResult();
                    result.setTitle(item.has("title") ? item.get("title").asText() : "");
                    result.setUrl(item.has("link") ? item.get("link").asText() : "");
                    result.setDescription(item.has("snippet") ? item.get("snippet").asText() : "");
                    result.setImageUrl(item.has("thumbnail") ? item.get("thumbnail").asText() : null);
                    searchResults.add(result);
                }
            }

            CachedQuery cacheEntry = new CachedQuery();
            cacheEntry.setQuery(query);
            cacheEntry.setResults(searchResults);
            cacheEntry.setCreatedAt(new Date());
            repo.save(cacheEntry);

            System.out.println("✅ Cached search results for: " + query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return searchResults;
    }

}
