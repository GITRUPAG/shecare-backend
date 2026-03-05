package com.app.shecare.controller;

import com.app.shecare.dto.CommentRequest;
import com.app.shecare.dto.CreatePostRequest;
import com.app.shecare.mongo.document.CommunityComment;
import com.app.shecare.mongo.document.CommunityPost;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.CommunityService;

import lombok.RequiredArgsConstructor;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping("/posts")
public CommunityPost createPost(
    @AuthenticationPrincipal CustomUserDetails userDetails,
    @RequestPart("data") CreatePostRequest request,
    @RequestPart(value = "file", required = false) MultipartFile file
) {
    return communityService.createPost(userDetails.getUser(), request, file);
}

    @GetMapping("/feed")
    public List<CommunityPost> getFeed(

            @RequestParam(required = false) String category,
            @RequestParam(required = false) String hashtag,
            @RequestParam(defaultValue = "0") int page

    ) {

        return communityService.getFeed(category, hashtag, page);

    }

    @PostMapping("/posts/{postId}/like")
    public String likePost(

            @PathVariable String postId,

            @AuthenticationPrincipal CustomUserDetails userDetails

    ) {

        return communityService.likePost(userDetails.getUser(), postId);

    }

    @PostMapping("/posts/{postId}/comment")
public CommunityComment addComment(

        @PathVariable String postId,

        @AuthenticationPrincipal CustomUserDetails userDetails,

        @RequestBody CommentRequest request
) {

    return communityService.addComment(
            userDetails.getUser(),
            postId,
            request
    );
}

    @PostMapping("/comments/{commentId}/reply")
public CommunityComment replyToComment(

        @PathVariable String commentId,

        @AuthenticationPrincipal CustomUserDetails userDetails,

        @RequestBody CommentRequest request

) {

    return communityService.replyToComment(
            userDetails.getUser(),
            commentId,
            request
    );
}

@GetMapping("/posts/{postId}/comments")
public List<CommunityComment> getComments(

        @PathVariable String postId

) {

    return communityService.getComments(postId);

}

@GetMapping("/my-posts")
public List<CommunityPost> getMyPosts(

        @AuthenticationPrincipal CustomUserDetails userDetails

) {

    return communityService.getMyPosts(userDetails.getUser());

}

@PostMapping("/posts/{postId}/repost")
public String repost(

        @PathVariable String postId,

        @AuthenticationPrincipal CustomUserDetails userDetails

) {

    return communityService.repost(
            userDetails.getUser(),
            postId
    );

}

@GetMapping("/search")
public List<CommunityPost> searchByHashtag(

        @RequestParam String hashtag

) {

    return communityService.searchByHashtag(hashtag);

}

@PostMapping("/posts/{postId}/report")
public String reportPost(

        @PathVariable String postId,

        @RequestParam String reason,

        @AuthenticationPrincipal CustomUserDetails userDetails

) {

    return communityService.reportPost(
            userDetails.getUser(),
            postId,
            reason
    );
}

@PostMapping("/posts/{postId}/bookmark")
public String bookmarkPost(

        @PathVariable String postId,

        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return communityService.bookmarkPost(
            userDetails.getUser(),
            postId
    );
}

@GetMapping("/bookmarks")
public List<CommunityPost> getBookmarks(

        @AuthenticationPrincipal CustomUserDetails userDetails
) {

    return communityService.getBookmarks(
            userDetails.getUser()
    );
}

@GetMapping("/trending")
public List<CommunityPost> trendingPosts(

        @RequestParam(defaultValue = "0") int page
) {

    return communityService.getTrendingPosts(page);
}

}