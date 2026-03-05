package com.app.shecare.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "community_likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityLike {

    @Id
    private String id;

    private String postId;

    private Long userId;

    private LocalDateTime createdAt;

}