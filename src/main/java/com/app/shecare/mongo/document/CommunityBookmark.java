package com.app.shecare.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;

import java.time.LocalDateTime;

@Document(collection = "community_bookmarks")
@CompoundIndex(name = "unique_bookmark", def = "{'postId':1,'userId':1}", unique = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommunityBookmark {

    @Id
    private String id;

    private String postId;

    private Long userId;

    private LocalDateTime createdAt;

}