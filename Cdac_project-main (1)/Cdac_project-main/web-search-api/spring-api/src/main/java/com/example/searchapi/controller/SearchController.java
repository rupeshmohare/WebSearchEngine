package com.example.searchapi.controller;

import com.example.searchapi.model.SearchResult;
import com.example.searchapi.model.ImageSearchResult;
import com.example.searchapi.service.SearchService;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SearchController {

    private final SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam String query) {
        return service.search(query);
    }

    @GetMapping("/search/images")
    public List<ImageSearchResult> searchImages(@RequestParam String query) {
        return service.searchImages(query);
    }
}
