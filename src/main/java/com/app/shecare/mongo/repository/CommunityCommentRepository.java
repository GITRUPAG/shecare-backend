package com.app.shecare.mongo.repository;

import com.app.shecare.mongo.document.CommunityComment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommunityCommentRepository
        extends MongoRepository<CommunityComment, String> {

    List<CommunityComment> findByPostId(String postId);

    List<CommunityComment> findByPostIdOrderByCreatedAtDesc(String postId);

    // replies of a comment
    List<CommunityComment> findByParentCommentIdOrderByCreatedAtAsc(String parentCommentId);

    void deleteByPostId(String postId);

}