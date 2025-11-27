package com.abc.social_service.service;

import com.abc.social_service.client.UserServiceClient;
import com.abc.social_service.dto.VoteRequest;
import com.abc.social_service.dto.VoteResponse;
import com.abc.social_service.entity.Comment;
import com.abc.social_service.entity.Vote;
import com.abc.social_service.exception.DuplicateVoteException;
import com.abc.social_service.exception.InvalidVoteTypeException;
import com.abc.social_service.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {
    private final VoteRepository voteRepository;
    private final CommentService commentService;
    private final UserServiceClient userServiceClient;
    private final VoteWeightCalculator voteWeightCalculator;

    @Transactional
    public VoteResponse voteOnComment(VoteRequest request) {
        // Validate vote type
        if (!isValidVoteType(request.getVoteType())) {
            throw new InvalidVoteTypeException();
        }
        
        // Check for duplicate vote
        if (voteRepository.existsByCommentIdAndUserId(request.getCommentId(), request.getUserId())) {
            throw new DuplicateVoteException();
        }
        
        // Verify comment exists
        Comment comment = commentService.getCommentEntityById(request.getCommentId());
        
        // Fetch user ELO rank
        Integer eloRank = userServiceClient.getUserEloRank(request.getUserId());
        log.debug("User {} has ELO rank {}", request.getUserId(), eloRank);
        
        // Calculate vote weight
        Double voteWeight = voteWeightCalculator.calculateWeight(eloRank);
        log.debug("Calculated vote weight {} for user {}", voteWeight, request.getUserId());
        
        // Create vote with weight and type
        Vote vote = new Vote();
        vote.setCommentId(request.getCommentId());
        vote.setUserId(request.getUserId());
        vote.setVoteType(request.getVoteType());
        vote.setVoteWeight(voteWeight);
        voteRepository.save(vote);
        
        // Update comment's weighted vote score
        double scoreChange = "USEFUL".equals(request.getVoteType()) ? voteWeight : -voteWeight;
        commentService.updateWeightedVoteScore(request.getCommentId(), scoreChange);
        
        // Increment vote count for backward compatibility
        commentService.incrementVoteCount(request.getCommentId());
        
        // Get updated comment
        Comment updatedComment = commentService.getCommentEntityById(request.getCommentId());
        
        return new VoteResponse(
            request.getCommentId(),
            updatedComment.getVoteCount(),
            voteWeight,
            updatedComment.getWeightedVoteScore(),
            updatedComment.getVotePercentage(),
            "Vote recorded successfully"
        );
    }
    
    private boolean isValidVoteType(String voteType) {
        return "USEFUL".equals(voteType) || "NOT_USEFUL".equals(voteType);
    }
}
