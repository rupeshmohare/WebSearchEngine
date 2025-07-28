package com.example.searchapi.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Document(collection = "cached_image_queries")
public class CachedImageQuery {
    @Id
    private String id;
    private String query;
    private List<ImageSearchResult> results;
    private Date createdAt;
}
