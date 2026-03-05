package com.app.shecare.mongo.repository;

import com.app.shecare.mongo.document.CommunityPost;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommunityPostRepository
        extends MongoRepository<CommunityPost, String> {

    List<CommunityPost> findByCategoryOrderByCreatedAtDesc(
            String category,
            Pageable pageable
    );

    List<CommunityPost> findByHashtagsContainingOrderByCreatedAtDesc(
            String hashtag,
            Pageable pageable
    );

    List<CommunityPost> findByHashtagsContainingOrderByCreatedAtDesc(String hashtag);

    List<CommunityPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<CommunityPost> findByUserId(Long userId);

    List<CommunityPost> findAllByOrderByScoreDescCreatedAtDesc(Pageable pageable);

}