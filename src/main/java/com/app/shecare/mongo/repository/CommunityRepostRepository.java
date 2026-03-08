package com.app.shecare.mongo.repository;

import com.app.shecare.mongo.document.CommunityRepost;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CommunityRepostRepository
        extends MongoRepository<CommunityRepost, String> {

    Optional<CommunityRepost> findByPostIdAndUserId(String postId, Long userId);

    void deleteByPostId(String postId);

}