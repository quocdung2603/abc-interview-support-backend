package com.abc.social_service.controller;

import com.abc.social_service.dto.PostRequest;
import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.service.PostService;
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

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management", description = "APIs for managing posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    @Operation(summary = "Create a new post")
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse response = postService.createPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all posts with pagination and filtering", 
               description = "Filter posts by field, topic, level, type, and status")
    public ResponseEntity<Page<PostResponse>> getAllPosts(
            @RequestParam(required = false) Long fieldId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Long levelId,
            @RequestParam(required = false) String postType,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        // If any classification filter is provided, use filterPosts
        if (fieldId != null || topicId != null || levelId != null) {
            com.abc.social_service.dto.PostFilterRequest filterRequest = 
                com.abc.social_service.dto.PostFilterRequest.builder()
                    .fieldId(fieldId)
                    .topicId(topicId)
                    .levelId(levelId)
                    .postType(postType)
                    .status(status)
                    .page(page)
                    .size(size)
                    .sortBy(sortBy)
                    .sortDirection(sortDirection)
                    .build();
            
            Page<PostResponse> posts = postService.filterPosts(filterRequest);
            return ResponseEntity.ok(posts);
        }
        
        // Otherwise use default getAllPosts
        Pageable pageable = PageRequest.of(page, size);
        Page<PostResponse> posts = postService.getAllPosts(pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a post by ID")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        PostResponse response = postService.getPostById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a post")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequest request) {
        PostResponse response = postService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a post (admin only)")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(Map.of("message", "Post deleted successfully", "id", id.toString()));
    }

    @PutMapping("/{id}/lock")
    @Operation(summary = "Set lock time for a post (admin only)")
    public ResponseEntity<PostResponse> setLockTime(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        LocalDateTime lockTime = LocalDateTime.parse(request.get("lockTime"));
        PostResponse response = postService.setLockTime(id, lockTime);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a draft post (admin only)")
    public ResponseEntity<PostResponse> approvePost(@PathVariable Long id) {
        try {
            PostResponse response = postService.approvePost(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
    }
    
    @PutMapping("/{id}/reject")
    @Operation(summary = "Reject a draft post (admin only)")
    public ResponseEntity<PostResponse> rejectPost(@PathVariable Long id) {
        try {
            PostResponse response = postService.rejectPost(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        }
    }
}
