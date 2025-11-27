package com.abc.social_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Feature: social-service-improvements, Property 3: Vote weight calculation
 * 
 * Property: For any user with an ELO rank, when voting on a comment, 
 * the calculated vote weight should follow the formula: 
 * weight = 1.0 + (eloRank - 1000) / 1000.0, bounded between 0.5 and 3.0
 * 
 * Validates: Requirements 4.2
 */
class VoteWeightCalculatorTest {

    private VoteWeightCalculator calculator;
    private final Random random = new Random();

    @BeforeEach
    void setUp() {
        calculator = new VoteWeightCalculator();
    }

    @Test
    void calculateWeight_BaselineElo_ReturnsOne() {
        // Given: ELO rank of 1000 (baseline)
        Integer eloRank = 1000;

        // When
        Double weight = calculator.calculateWeight(eloRank);

        // Then
        assertThat(weight).isEqualTo(1.0);
    }

    @Test
    void calculateWeight_HighElo_ReturnsHigherWeight() {
        // Given: ELO rank of 1500
        Integer eloRank = 1500;

        // When
        Double weight = calculator.calculateWeight(eloRank);

        // Then
        assertThat(weight).isEqualTo(1.5);
    }

    @Test
    void calculateWeight_VeryHighElo_CapsAtMaximum() {
        // Given: ELO rank of 5000 (very high)
        Integer eloRank = 5000;

        // When
        Double weight = calculator.calculateWeight(eloRank);

        // Then: Should be capped at 3.0
        assertThat(weight).isEqualTo(3.0);
    }

    @Test
    void calculateWeight_VeryLowElo_CapsAtMinimum() {
        // Given: ELO rank of 0 (very low)
        Integer eloRank = 0;

        // When
        Double weight = calculator.calculateWeight(eloRank);

        // Then: Should be capped at 0.5
        assertThat(weight).isEqualTo(0.5);
    }

    @Test
    void calculateWeight_NullElo_ReturnsDefaultWeight() {
        // Given: null ELO rank
        Integer eloRank = null;

        // When
        Double weight = calculator.calculateWeight(eloRank);

        // Then: Should return default weight of 1.0
        assertThat(weight).isEqualTo(1.0);
    }

    @RepeatedTest(100)
    void calculateWeight_RandomElo_FollowsFormula() {
        // Given: Random ELO rank between 500 and 2500
        Integer eloRank = 500 + random.nextInt(2000);

        // When
        Double weight = calculator.calculateWeight(eloRank);

        // Then: Weight should be within bounds
        assertThat(weight).isBetween(0.5, 3.0);

        // And: Weight should follow formula (if not bounded)
        double expectedWeight = 1.0 + (eloRank - 1000) / 1000.0;
        double boundedExpected = Math.min(3.0, Math.max(0.5, expectedWeight));
        assertThat(weight).isEqualTo(boundedExpected);
    }
}
