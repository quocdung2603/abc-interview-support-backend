package com.abc.social_service.property;

import com.abc.social_service.entity.Vote;
import com.abc.social_service.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: social-service-improvements, Property 14: Vote weight persistence
 * 
 * Property: For any vote, the vote weight calculated at the time of voting 
 * should be stored in the database and retrievable
 * 
 * Validates: Requirements 4.5
 */
@DataJpaTest
public class VoteWeightPersistencePropertyTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VoteRepository voteRepository;

    private final Random random = new Random();
    private final String[] voteTypes = {"USEFUL", "NOT_USEFUL"};

    @BeforeEach
    void setUp() {
        voteRepository.deleteAll();
    }

    @RepeatedTest(100)
    void voteWeightShouldBePersisted() {
        // Given: Random test data
        Long commentId = 1L + random.nextLong(1000);
        Long userId = 1L + random.nextLong(1000);
        Double voteWeight = 0.5 + (random.nextDouble() * 2.5); // 0.5 to 3.0
        String voteType = voteTypes[random.nextInt(voteTypes.length)];
        
        // Given: A vote with a specific weight
        Vote vote = new Vote();
        vote.setCommentId(commentId);
        vote.setUserId(userId);
        vote.setVoteWeight(voteWeight);
        vote.setVoteType(voteType);
        
        // When: The vote is saved to the database
        Vote savedVote = voteRepository.save(vote);
        entityManager.flush();
        entityManager.clear();
        
        // Then: The vote weight should be retrievable and match the original
        Vote retrievedVote = voteRepository.findById(savedVote.getId()).orElseThrow();
        assertThat(retrievedVote.getVoteWeight())
                .isNotNull()
                .isEqualTo(voteWeight);
        assertThat(retrievedVote.getVoteType()).isEqualTo(voteType);
    }
}
