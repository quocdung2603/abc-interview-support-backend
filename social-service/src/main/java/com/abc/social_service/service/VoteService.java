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
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${elo.points.vote-useful:2}")
    private Integer eloPointsVoteUseful;
    
    @Value("${elo.points.vote-unuseful:1}")
    private Integer eloPointsVoteUnuseful;
    
    @Value("${elo.points.receive-useful-vote:5}")
    private Integer eloPointsReceiveUseful;
    
    @Value("${elo.points.receive-unuseful-vote:-2}")
    private Integer eloPointsReceiveUnuseful;

    @Transactional
    public VoteResponse voteOnComment(VoteRequest request) {
        // Get effective vote type from request
        String effectiveVoteType = request.getEffectiveVoteType();
        
        // Validate vote type
        if (!isValidVoteType(effectiveVoteType)) {
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
        vote.setVoteType(effectiveVoteType);
        vote.setVoteWeight(voteWeight);
        voteRepository.save(vote);
        
        // Update comment's weighted vote score
        double scoreChange = "USEFUL".equals(effectiveVoteType) ? voteWeight : -voteWeight;
        commentService.updateWeightedVoteScore(request.getCommentId(), scoreChange);
        
        // Increment vote count for backward compatibility
        commentService.incrementVoteCount(request.getCommentId());
        
        // Update ELO points for voter
        updateVoterElo(request.getUserId(), effectiveVoteType);
        
        // Update ELO points for comment author
        updateCommentAuthorElo(comment.getUserId(), effectiveVoteType);
        
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
    
    /**
     * Update ELO points for the user who voted
     */
    private void updateVoterElo(Long voterId, String voteType) {
        try {
            int points = "USEFUL".equals(voteType) ? eloPointsVoteUseful : eloPointsVoteUnuseful;
            String action = "USEFUL".equals(voteType) ? "VOTE_USEFUL" : "VOTE_UNUSEFUL";
            String description = String.format("Voted %s on a comment", voteType.toLowerCase());
            
            userServiceClient.applyEloPoints(voterId, action, points, description);
            log.info("Applied {} ELO points to voter {} for {} vote", points, voterId, voteType);
        } catch (Exception e) {
            log.warn("Failed to update ELO for voter {}: {}", voterId, e.getMessage());
            // Don't fail the vote operation if ELO update fails
        }
    }
    
    /**
     * Update ELO points for the comment author who received the vote
     */
    private void updateCommentAuthorElo(Long authorId, String voteType) {
        try {
            int points = "USEFUL".equals(voteType) ? eloPointsReceiveUseful : eloPointsReceiveUnuseful;
            String action = "USEFUL".equals(voteType) ? "RECEIVE_USEFUL_VOTE" : "RECEIVE_UNUSEFUL_VOTE";
            String description = String.format("Received %s vote on comment", voteType.toLowerCase());
            
            userServiceClient.applyEloPoints(authorId, action, points, description);
            log.info("Applied {} ELO points to comment author {} for receiving {} vote", points, authorId, voteType);
        } catch (Exception e) {
            log.warn("Failed to update ELO for comment author {}: {}", authorId, e.getMessage());
            // Don't fail the vote operation if ELO update fails
        }
    }
    
    private boolean isValidVoteType(String voteType) {
        return "USEFUL".equals(voteType) || "NOT_USEFUL".equals(voteType);
    }
}
