package com.abc.social_service.service;

import com.abc.social_service.dto.PostRequest;
import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.entity.Post;
import com.abc.social_service.exception.PostNotFoundException;
import com.abc.social_service.mapper.PostMapper;
import com.abc.social_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;

    @Transactional
    public PostResponse createPost(PostRequest request) {
        Post post = postMapper.toEntity(request);
        Post savedPost = postRepository.save(post);
        return postMapper.toResponse(savedPost);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return postMapper.toResponse(post);
    }

    public Post getPostEntityById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return posts.map(postMapper::toResponse);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        postMapper.updateEntityFromRequest(request, post);
        Post updatedPost = postRepository.save(post);
        return postMapper.toResponse(updatedPost);
    }

    @Transactional
    public void deletePost(Long id) {
        if (!postRepository.existsById(id)) {
            throw new PostNotFoundException(id);
        }
        postRepository.deleteById(id);
    }

    @Transactional
    public PostResponse setLockTime(Long id, LocalDateTime lockTime) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.setLockTime(lockTime);
        Post updatedPost = postRepository.save(post);
        return postMapper.toResponse(updatedPost);
    }

    public boolean isLocked(Post post) {
        if (post.getLockTime() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(post.getLockTime()) || 
               LocalDateTime.now().isEqual(post.getLockTime());
    }
}
