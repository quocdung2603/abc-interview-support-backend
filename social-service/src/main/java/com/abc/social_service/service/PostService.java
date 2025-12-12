package com.abc.social_service.service;

import com.abc.social_service.client.QuestionServiceClient;
import com.abc.social_service.dto.FieldResponse;
import com.abc.social_service.dto.LevelResponse;
import com.abc.social_service.dto.PostFilterRequest;
import com.abc.social_service.dto.PostRequest;
import com.abc.social_service.dto.PostResponse;
import com.abc.social_service.dto.TopicResponse;
import com.abc.social_service.entity.Post;
import com.abc.social_service.exception.InvalidClassificationException;
import com.abc.social_service.exception.PostNotFoundException;
import com.abc.social_service.mapper.PostMapper;
import com.abc.social_service.repository.PostRepository;
import com.abc.social_service.util.AuthenticationUtil;
import com.abc.social_service.validation.ClassificationValidator;
import com.abc.social_service.validation.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final ClassificationValidator classificationValidator;
    private final ClassificationCacheService cacheService;
    private final QuestionServiceClient questionServiceClient;

    @Transactional
    public PostResponse createPost(PostRequest request) {
        ValidationResult validationResult = classificationValidator.validateClassification(
            request.getFieldId(), request.getTopicId(), request.getLevelId()
        );
        
        if (!validationResult.isValid()) {
            throw new InvalidClassificationException(
                validationResult.getErrorMessage(), validationResult.getFieldErrors()
            );
        }
        
        Post post = postMapper.toEntity(request);
        Long userId = AuthenticationUtil.getUserId();
        if (userId == null) {
            throw new RuntimeException("User ID not found in authentication context");
        }
        post.setUserId(userId);
        
        String userRole = AuthenticationUtil.getUserRole();
        if ("ADMIN".equals(userRole)) {
            post.setStatus("DRAFT");
        } else {
            post.setStatus("DRAFT");
        }
        
        Post savedPost = postRepository.save(post);
        return enrichResponse(savedPost);
    }
    
    @Transactional
    public PostResponse createPost(PostRequest request, String userRole, Long userId) {
        ValidationResult validationResult = classificationValidator.validateClassification(
            request.getFieldId(), request.getTopicId(), request.getLevelId()
        );
        
        if (!validationResult.isValid()) {
            throw new InvalidClassificationException(
                validationResult.getErrorMessage(), validationResult.getFieldErrors()
            );
        }
        
        Post post = postMapper.toEntity(request);
        post.setUserId(userId);
        
        if ("ADMIN".equals(userRole)) {
            post.setStatus("DRAFT");
        } else {
            post.setStatus("DRAFT");
        }
        
        Post savedPost = postRepository.save(post);
        return enrichResponse(savedPost);
    }

    public PostResponse getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return enrichResponse(post);
    }

    public Post getPostEntityById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public Page<PostResponse> getAllPosts(Pageable pageable) {
        String userRole = AuthenticationUtil.getUserRole();
        Long userId = AuthenticationUtil.getUserId();
        return getAllPosts(pageable, userRole, userId);
    }
    
    public Page<PostResponse> getAllPosts(Pageable pageable, String userRole, Long userId) {
        // Return all posts regardless of status (PUBLISHED, DRAFT, LOCKED)
        Page<Post> posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return posts.map(this::enrichResponse);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        ValidationResult validationResult = classificationValidator.validateClassification(
            request.getFieldId(), request.getTopicId(), request.getLevelId()
        );
        
        if (!validationResult.isValid()) {
            throw new InvalidClassificationException(
                validationResult.getErrorMessage(), validationResult.getFieldErrors()
            );
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        postMapper.updateEntityFromRequest(request, post);
        Post updatedPost = postRepository.save(post);
        return enrichResponse(updatedPost);
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
        return enrichResponse(updatedPost);
    }

    public boolean isLocked(Post post) {
        if (post.getLockTime() == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(post.getLockTime()) || 
               LocalDateTime.now().isEqual(post.getLockTime());
    }
    
    @Transactional
    public PostResponse approvePost(Long id) {
        if (!AuthenticationUtil.isAdmin()) {
            throw new RuntimeException("Only administrators can approve posts");
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        
        if (!"DRAFT".equals(post.getStatus())) {
            throw new RuntimeException("Only draft posts can be approved");
        }
        
        post.setStatus("PUBLISHED");
        Post updatedPost = postRepository.save(post);
        return enrichResponse(updatedPost);
    }
    
    @Transactional
    public PostResponse approvePost(Long id, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new RuntimeException("Only administrators can approve posts");
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        
        if (!"DRAFT".equals(post.getStatus())) {
            throw new RuntimeException("Only draft posts can be approved");
        }
        
        post.setStatus("PUBLISHED");
        Post updatedPost = postRepository.save(post);
        return enrichResponse(updatedPost);
    }
    
    @Transactional
    public PostResponse rejectPost(Long id) {
        if (!AuthenticationUtil.isAdmin()) {
            throw new RuntimeException("Only administrators can reject posts");
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        
        if (!"DRAFT".equals(post.getStatus())) {
            throw new RuntimeException("Only draft posts can be rejected");
        }
        
        post.setStatus("REJECTED");
        Post updatedPost = postRepository.save(post);
        return enrichResponse(updatedPost);
    }
    
    @Transactional
    public PostResponse rejectPost(Long id, String userRole) {
        if (!"ADMIN".equals(userRole)) {
            throw new RuntimeException("Only administrators can reject posts");
        }
        
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        
        if (!"DRAFT".equals(post.getStatus())) {
            throw new RuntimeException("Only draft posts can be rejected");
        }
        
        post.setStatus("REJECTED");
        Post updatedPost = postRepository.save(post);
        return enrichResponse(updatedPost);
    }
    
    private PostResponse enrichResponse(Post post) {
        try {
            FieldResponse field = null;
            TopicResponse topic = null;
            LevelResponse level = null;
            
            if (post.getFieldId() != null) {
                try {
                    var cachedField = cacheService.getCachedField(post.getFieldId());
                    if (cachedField.isPresent()) {
                        field = cachedField.get();
                    } else {
                        field = questionServiceClient.getFieldById(post.getFieldId());
                        if (field != null) {
                            cacheService.cacheField(post.getFieldId(), field);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch field {}: {}", post.getFieldId(), e.getMessage());
                }
            }
            
            if (post.getTopicId() != null) {
                try {
                    var cachedTopic = cacheService.getCachedTopic(post.getTopicId());
                    if (cachedTopic.isPresent()) {
                        topic = cachedTopic.get();
                    } else {
                        topic = questionServiceClient.getTopicById(post.getTopicId());
                        if (topic != null) {
                            cacheService.cacheTopic(post.getTopicId(), topic);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch topic {}: {}", post.getTopicId(), e.getMessage());
                }
            }
            
            if (post.getLevelId() != null) {
                try {
                    var cachedLevel = cacheService.getCachedLevel(post.getLevelId());
                    if (cachedLevel.isPresent()) {
                        level = cachedLevel.get();
                    } else {
                        level = questionServiceClient.getLevelById(post.getLevelId());
                        if (level != null) {
                            cacheService.cacheLevel(post.getLevelId(), level);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch level {}: {}", post.getLevelId(), e.getMessage());
                }
            }
            
            return postMapper.toResponse(post, field, topic, level);
            
        } catch (Exception e) {
            log.error("Error enriching post response: {}", e.getMessage());
            return postMapper.toResponse(post);
        }
    }
    
    public Page<PostResponse> filterPosts(PostFilterRequest filterRequest) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(
            filterRequest.getPage(),
            filterRequest.getSize(),
            org.springframework.data.domain.Sort.Direction.fromString(filterRequest.getSortDirection()),
            filterRequest.getSortBy()
        );
        
        Page<Post> posts;
        
        // Determine which repository method to call based on provided filters
        if (filterRequest.getFieldId() != null && filterRequest.getTopicId() != null && filterRequest.getLevelId() != null) {
            posts = postRepository.findByFieldIdAndTopicIdAndLevelId(
                filterRequest.getFieldId(), 
                filterRequest.getTopicId(), 
                filterRequest.getLevelId(), 
                pageable
            );
        } else if (filterRequest.getFieldId() != null && filterRequest.getTopicId() != null) {
            posts = postRepository.findByFieldIdAndTopicId(
                filterRequest.getFieldId(), 
                filterRequest.getTopicId(), 
                pageable
            );
        } else if (filterRequest.getFieldId() != null && filterRequest.getLevelId() != null) {
            posts = postRepository.findByFieldIdAndLevelId(
                filterRequest.getFieldId(), 
                filterRequest.getLevelId(), 
                pageable
            );
        } else if (filterRequest.getTopicId() != null && filterRequest.getLevelId() != null) {
            posts = postRepository.findByTopicIdAndLevelId(
                filterRequest.getTopicId(), 
                filterRequest.getLevelId(), 
                pageable
            );
        } else if (filterRequest.getFieldId() != null) {
            posts = postRepository.findByFieldId(filterRequest.getFieldId(), pageable);
        } else if (filterRequest.getTopicId() != null) {
            posts = postRepository.findByTopicId(filterRequest.getTopicId(), pageable);
        } else if (filterRequest.getLevelId() != null) {
            posts = postRepository.findByLevelId(filterRequest.getLevelId(), pageable);
        } else {
            // No classification filters, return all posts
            posts = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        
        return posts.map(this::enrichResponse);
    }

}
