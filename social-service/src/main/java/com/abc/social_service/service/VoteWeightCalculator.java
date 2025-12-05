package com.abc.social_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for calculating vote weight based on user ELO rank with clear tier differentiation
 * 
 * ELO Tiers and Weights:
 * - Beginner (0-999):     0.5x weight
 * - Novice (1000-1199):   0.8x weight
 * - Intermediate (1200-1399): 1.0x weight (baseline)
 * - Advanced (1400-1599): 1.3x weight
 * - Expert (1600-1799):   1.6x weight
 * - Master (1800-1999):   2.0x weight
 * - Grandmaster (2000+):  2.5x weight
 */
@Service
@Slf4j
public class VoteWeightCalculator {
    
    // ELO tier thresholds
    private static final int TIER_BEGINNER = 0;
    private static final int TIER_NOVICE = 1000;
    private static final int TIER_INTERMEDIATE = 1200;
    private static final int TIER_ADVANCED = 1400;
    private static final int TIER_EXPERT = 1600;
    private static final int TIER_MASTER = 1800;
    private static final int TIER_GRANDMASTER = 2000;
    
    // Vote weights for each tier
    private static final double WEIGHT_BEGINNER = 0.5;
    private static final double WEIGHT_NOVICE = 0.8;
    private static final double WEIGHT_INTERMEDIATE = 1.0;
    private static final double WEIGHT_ADVANCED = 1.3;
    private static final double WEIGHT_EXPERT = 1.6;
    private static final double WEIGHT_MASTER = 2.0;
    private static final double WEIGHT_GRANDMASTER = 2.5;
    
    /**
     * Calculates vote weight based on ELO rank using tier system
     * 
     * @param eloRank User's ELO ranking (null will use baseline)
     * @return Vote weight based on tier
     */
    public Double calculateWeight(Integer eloRank) {
        if (eloRank == null) {
            log.debug("ELO rank is null, using baseline weight {}", WEIGHT_INTERMEDIATE);
            return WEIGHT_INTERMEDIATE;
        }
        
        double weight;
        String tier;
        
        if (eloRank >= TIER_GRANDMASTER) {
            weight = WEIGHT_GRANDMASTER;
            tier = "Grandmaster";
        } else if (eloRank >= TIER_MASTER) {
            weight = WEIGHT_MASTER;
            tier = "Master";
        } else if (eloRank >= TIER_EXPERT) {
            weight = WEIGHT_EXPERT;
            tier = "Expert";
        } else if (eloRank >= TIER_ADVANCED) {
            weight = WEIGHT_ADVANCED;
            tier = "Advanced";
        } else if (eloRank >= TIER_INTERMEDIATE) {
            weight = WEIGHT_INTERMEDIATE;
            tier = "Intermediate";
        } else if (eloRank >= TIER_NOVICE) {
            weight = WEIGHT_NOVICE;
            tier = "Novice";
        } else {
            weight = WEIGHT_BEGINNER;
            tier = "Beginner";
        }
        
        log.debug("ELO rank {} falls in {} tier with vote weight {}", eloRank, tier, weight);
        
        return weight;
    }
}
