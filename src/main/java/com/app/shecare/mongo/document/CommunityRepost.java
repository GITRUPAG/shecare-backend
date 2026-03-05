package com.app.shecare.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "community_reposts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityRepost {

    @Id
    private String id;

    // post reference
    @Indexed
    private String postId;

    // user who reposted
    @Indexed
    private Long userId;

    private LocalDateTime createdAt;

}