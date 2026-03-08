package com.app.shecare.controller;

import com.app.shecare.dto.CommentRequest;
import com.app.shecare.dto.CreatePostRequest;
import com.app.shecare.mongo.document.CommunityComment;
import com.app.shecare.mongo.document.CommunityPost;
import com.app.shecare.security.CustomUserDetails;
import com.app.shecare.service.CommunityService;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public CommunityPost createPost(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam String content,
        @RequestParam boolean anonymous,
        @RequestParam String category,
        @RequestParam(required = false) List<String> hashtags,
        @RequestPart(value = "file", required = false) MultipartFile file
) {
        if (hashtags == null) {
    hashtags = new ArrayList<>();
}

    CreatePostRequest request = new CreatePostRequest();
    request.setContent(content);
    request.setAnonymous(anonymous);
    request.setCategory(category);
    request.setHashtags(hashtags);

    return communityService.createPost(userDetails.getUser(), request, file);
}

    @GetMapping("/feed")
public List<CommunityPost> getFeed(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String hashtag
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

@DeleteMapping("/posts/{postId}")
public String deletePost(
        @PathVariable String postId,
        @AuthenticationPrincipal CustomUserDetails userDetails
) {
    return communityService.deletePost(userDetails.getUser(), postId);
}

@PutMapping(value = "/posts/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public CommunityPost editPost(
        @PathVariable String postId,
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestParam String content,
        @RequestParam String category,
        @RequestParam boolean anonymous,
        @RequestParam(required = false) List<String> hashtags,
        @RequestPart(value = "file", required = false) MultipartFile file
) {
    if (hashtags == null) hashtags = new ArrayList<>();

    CreatePostRequest request = new CreatePostRequest();
    request.setContent(content);
    request.setCategory(category);
    request.setAnonymous(anonymous);
    request.setHashtags(hashtags);

    return communityService.editPost(userDetails.getUser(), postId, request, file);
}

// Already exists as GET /bookmarks — just make sure it's wired:
// @GetMapping("/bookmarks") is already in your controller ✅

}