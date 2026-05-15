package com.app.shecare.mongo.repository;

import com.app.shecare.mongo.document.CommunityBookmark;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CommunityBookmarkRepository
        extends MongoRepository<CommunityBookmark, String> {

    Optional<CommunityBookmark> findByPostIdAndUserId(String postId, Long userId);

    List<CommunityBookmark> findByUserId(Long userId);

    void deleteByPostId(String postId);

}