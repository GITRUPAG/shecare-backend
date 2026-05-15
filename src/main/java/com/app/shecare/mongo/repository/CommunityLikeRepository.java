package com.app.shecare.mongo.repository;

import com.app.shecare.mongo.document.CommunityLike;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CommunityLikeRepository
        extends MongoRepository<CommunityLike, String> {

    Optional<CommunityLike> findByPostIdAndUserId(String postId, Long userId);

    long countByPostId(String postId);

    void deleteByPostId(String postId);

}