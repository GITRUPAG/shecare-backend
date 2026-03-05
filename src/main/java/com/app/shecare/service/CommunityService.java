package com.app.shecare.service;

import com.app.shecare.dto.CommentRequest;
import com.app.shecare.dto.CreatePostRequest;
import com.app.shecare.mongo.document.CommunityBookmark;
import com.app.shecare.mongo.document.CommunityComment;
import com.app.shecare.mongo.document.CommunityLike;
import com.app.shecare.mongo.document.CommunityPost;
import com.app.shecare.mongo.document.CommunityReport;
import com.app.shecare.mongo.document.CommunityRepost;
import com.app.shecare.mongo.repository.CommunityLikeRepository;
import com.app.shecare.mongo.repository.CommunityPostRepository;
import com.app.shecare.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

import com.app.shecare.mongo.repository.CommunityBookmarkRepository;
import com.app.shecare.mongo.repository.CommunityCommentRepository;
import com.app.shecare.mongo.repository.CommunityReportRepository;
import com.app.shecare.mongo.repository.CommunityRepostRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityLikeRepository likeRepository;
    private final CommunityCommentRepository commentRepository;
    private final CommunityReportRepository reportRepository;
    private final CommunityRepostRepository repostRepository;
    private final CommunityBookmarkRepository bookmarkRepository;
    private final AiPredictionService aiPredictionService;
    private final CloudinaryService cloudinaryService;

    public CommunityPost createPost(
            User user,
            CreatePostRequest request,
            MultipartFile file
    ) {

        String mediaUrl = null;
        String mediaType = null;

        // Upload media if exists
        if (file != null && !file.isEmpty()) {

            mediaUrl = cloudinaryService.uploadImage(file);

            String contentType = file.getContentType();

            if (contentType != null && contentType.startsWith("video")) {
                mediaType = "VIDEO";
            } else {
                mediaType = "IMAGE";
            }
        }

        // AI toxicity check
        var toxicity = aiPredictionService.checkToxicity(request.getContent());

        if (toxicity.is_toxic()) {

            if ("critical".equalsIgnoreCase(toxicity.getSeverity())) {

                reportRepository.save(
                        CommunityReport.builder()
                                .reportedBy(user.getId())
                                .reason("AI detected harassment")
                                .createdAt(LocalDateTime.now())
                                .build()
                );

                throw new RuntimeException("Post blocked due to harmful content");
            }
        }

        CommunityPost post = CommunityPost.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .anonymous(request.isAnonymous())
                .content(request.getContent())
                .hashtags(request.getHashtags())
                .category(request.getCategory())
                .mediaUrls(mediaUrl != null ? List.of(mediaUrl) : null)
                .mediaType(mediaType)
                .createdAt(LocalDateTime.now())
                .likeCount(0)
                .commentCount(0)
                .repostCount(0)
                .score(0)
                .build();

        return postRepository.save(post);
    }
    

    

    public List<CommunityPost> getFeed(String category, String hashtag, int page) {

        Pageable pageable = PageRequest.of(page, 10);

        if (category != null) {
            return postRepository
                    .findByCategoryOrderByCreatedAtDesc(category, pageable);
        }

        if (hashtag != null) {
            return postRepository
                    .findByHashtagsContainingOrderByCreatedAtDesc(hashtag, pageable);
        }

        return postRepository
                .findAllByOrderByCreatedAtDesc(pageable);
    }

    public String likePost(User user, String postId) {

        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        var existingLike = likeRepository
                .findByPostIdAndUserId(postId, user.getId());

        if (existingLike.isPresent()) {

            likeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
            post.setScore(post.getScore() + 3);
            postRepository.save(post);

            return "Post unliked";

        } else {

            CommunityLike like = CommunityLike.builder()
                    .postId(postId)
                    .userId(user.getId())
                    .createdAt(LocalDateTime.now())
                    .build();

            likeRepository.save(like);

            post.setLikeCount(post.getLikeCount() + 1);
            post.setScore(post.getScore() + 3);
            postRepository.save(post);

            return "Post liked";
        }
    }

    public CommunityComment addComment(User user, String postId, CommentRequest request) {

        // ── Toxicity check ──────────────────────────────────────────────────
        var toxicity = aiPredictionService.checkToxicity(request.getContent());

        if (toxicity.is_toxic()) {

            if ("critical".equalsIgnoreCase(toxicity.getSeverity())) {

                // Auto-report the user for critical violations
                reportRepository.save(
                    CommunityReport.builder()
                        .reportedBy(user.getId())
                        .reason("AI detected harassment in comment")
                        .createdAt(LocalDateTime.now())
                        .build()
                );

                throw new RuntimeException("Comment blocked due to harmful language");
            }

            if ("medium".equalsIgnoreCase(toxicity.getSeverity())) {
                // TODO: store in moderation queue if needed
            }
        }
        // ────────────────────────────────────────────────────────────────────

        CommunityComment comment = CommunityComment.builder()
                .postId(postId)
                .userId(user.getId())
                .username(user.getUsername())
                .anonymous(request.isAnonymous())
                .content(request.getContent())
                .parentCommentId(null)
                .createdAt(LocalDateTime.now())
                .build();

        return commentRepository.save(comment);
    }

    public CommunityComment replyToComment(
            User user,
            String commentId,
            CommentRequest request
    ) {

        CommunityComment parent = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        CommunityComment reply = CommunityComment.builder()
                .postId(parent.getPostId())
                .userId(user.getId())
                .username(user.getUsername())
                .anonymous(request.isAnonymous())
                .content(request.getContent())
                .parentCommentId(commentId)
                .createdAt(LocalDateTime.now())
                .build();

        return commentRepository.save(reply);
    }

    public List<CommunityPost> getMyPosts(User user) {

        return postRepository.findByUserId(user.getId());
    }

    public List<CommunityPost> searchByHashtag(String hashtag) {

        return postRepository
                .findByHashtagsContainingOrderByCreatedAtDesc(hashtag);
    }

    public String reportPost(User user, String postId, String reason) {

        CommunityReport report = CommunityReport.builder()
                .postId(postId)
                .reportedBy(user.getId())
                .reason(reason)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);

        return "Post reported successfully";
    }

    public List<CommunityComment> getComments(String postId) {

        return commentRepository
                .findByPostIdOrderByCreatedAtDesc(postId);
    }

    public String repost(User user, String postId) {

        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        var existing = repostRepository
                .findByPostIdAndUserId(postId, user.getId());

        if (existing.isPresent()) {

            repostRepository.delete(existing.get());
            post.setRepostCount(post.getRepostCount() - 1);
            postRepository.save(post);

            return "Repost removed";

        } else {

            CommunityRepost repost = CommunityRepost.builder()
                    .postId(postId)
                    .userId(user.getId())
                    .createdAt(LocalDateTime.now())
                    .build();

            repostRepository.save(repost);

            post.setRepostCount(post.getRepostCount() + 1);
            postRepository.save(post);

            return "Post reposted";
        }
    }

    public String bookmarkPost(User user, String postId) {

        var existing = bookmarkRepository
                .findByPostIdAndUserId(postId, user.getId());

        if (existing.isPresent()) {

            bookmarkRepository.delete(existing.get());
            return "Bookmark removed";

        } else {

            CommunityBookmark bookmark = CommunityBookmark.builder()
                    .postId(postId)
                    .userId(user.getId())
                    .createdAt(LocalDateTime.now())
                    .build();

            bookmarkRepository.save(bookmark);

            return "Post bookmarked";
        }
    }

    public List<CommunityPost> getBookmarks(User user) {

        List<CommunityBookmark> bookmarks =
                bookmarkRepository.findByUserId(user.getId());

        List<String> postIds =
                bookmarks.stream()
                        .map(CommunityBookmark::getPostId)
                        .toList();

        return postRepository.findAllById(postIds);
    }

    public List<CommunityPost> getTrendingPosts(int page) {

        Pageable pageable = PageRequest.of(page, 10);

        return postRepository
                .findAllByOrderByScoreDescCreatedAtDesc(pageable);
    }
}