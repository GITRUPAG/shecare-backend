package com.app.shecare.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "community_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityComment {

    @Id
    private String id;

    // post reference
    @Indexed
    private String postId;

    // user info
    private Long userId;

    private String username;

    private boolean anonymous;

    // comment text
    private String content;

    // reply feature
    private String parentCommentId;

    @CreatedDate
    private LocalDateTime createdAt;

}