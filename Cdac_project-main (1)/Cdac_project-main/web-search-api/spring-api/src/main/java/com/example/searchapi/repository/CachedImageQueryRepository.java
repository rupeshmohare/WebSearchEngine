package com.example.searchapi.repository;

import com.example.searchapi.model.CachedImageQuery;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CachedImageQueryRepository extends MongoRepository<CachedImageQuery, String> {
    Optional<CachedImageQuery> findByQuery(String query);
}
