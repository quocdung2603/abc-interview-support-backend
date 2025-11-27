package com.abc.social_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for calculating vote weight based on user ELO rank
 * 
 * Formula: weight = 1.0 + (eloRank - 1000) / 1000.0
 * - ELO 1000 = weight 1.0 (baseline)
 * - ELO 1500 = weight 1.5
 * - ELO 2000 = weight 2.0
 * 
 * Weight is bounded between 0.5 and 3.0
 */
@Service
@Slf4j
public class VoteWeightCalculator {
    
    private static final double MIN_WEIGHT = 0.5;
    private static final double MAX_WEIGHT = 3.0;
    private static final int BASELINE_ELO = 1000;
    private static final double WEIGHT_FACTOR = 1000.0;
    
    /**
     * Calculates vote weight based on ELO rank
     * 
     * @param eloRank User's ELO ranking (null will use baseline)
     * @return Vote weight (minimum 0.5, maximum 3.0)
     */
    public Double calculateWeight(Integer eloRank) {
        if (eloRank == null) {
            log.debug("ELO rank is null, using baseline weight 1.0");
            return 1.0;
        }
        
        // Apply formula: weight = 1.0 + (eloRank - 1000) / 1000.0
        double weight = 1.0 + (eloRank - BASELINE_ELO) / WEIGHT_FACTOR;
        
        // Apply bounds
        double boundedWeight = Math.min(MAX_WEIGHT, Math.max(MIN_WEIGHT, weight));
        
        log.debug("Calculated weight {} for ELO rank {} (bounded from {})", 
                boundedWeight, eloRank, weight);
        
        return boundedWeight;
    }
}
