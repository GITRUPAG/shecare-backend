package com.app.shecare.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "community_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityPost {

    @Id
    private String id;

    // user reference
    private Long userId;

    private String username;

    private boolean anonymous;

    // post content
    private String content;

    // images / videos stored in Cloudinary
    private List<String> mediaUrls;

    private String mediaType; // IMAGE or VIDEO

    // hashtags
    @Indexed
    private List<String> hashtags;

    // category
    @Indexed
    private String category;

    // counters
    private int likeCount;

    private int commentCount;

    private int repostCount;

    private LocalDateTime createdAt;

    private int score;

    private LocalDateTime editedAt;

}