package com.abc.social_service.controller;

import com.abc.social_service.dto.CommentRequest;
import com.abc.social_service.dto.CommentResponse;
import com.abc.social_service.dto.CommentUpdateRequest;
import com.abc.social_service.dto.VoteRequest;
import com.abc.social_service.dto.VoteResponse;
import com.abc.social_service.service.CommentService;
import com.abc.social_service.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "APIs for managing comments and voting")
public class CommentController {
    private final CommentService commentService;
    private final VoteService voteService;

    @PostMapping
    @Operation(summary = "Create a new comment")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest request) {
        CommentResponse response = commentService.createComment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get all comments for a post (sorted by votes if locked, by time if not)")
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/post/{postId}/paginated")
    @Operation(summary = "Get comments for a post with pagination")
    public ResponseEntity<Page<CommentResponse>> getCommentsByPostIdPaginated(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getCommentsByPostIdPaginated(postId, pageable);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a comment by ID")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable Long id) {
        CommentResponse response = commentService.getCommentById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a comment (admin only)")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(Map.of("message", "Comment deleted successfully", "id", id.toString()));
    }

    @PostMapping("/{id}/vote")
    @Operation(summary = "Vote on a comment (USEFUL or NOT_USEFUL)")
    public ResponseEntity<VoteResponse> voteOnComment(
            @PathVariable Long id,
            @Valid @RequestBody VoteRequest request) {
        request.setCommentId(id);
        VoteResponse response = voteService.voteOnComment(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a comment (limited to 1 edit per comment on locked posts)")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest request) {
        CommentResponse response = commentService.updateComment(id, request);
        return ResponseEntity.ok(response);
    }
}
