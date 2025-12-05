package com.abc.question_service.service;

import com.abc.question_service.dto.InitializationResult;

/**
 * Service interface for database initialization operations.
 * Handles resetting and seeding reference data for bulk question generation.
 */
public interface DatabaseInitializationService {
    
    /**
     * Resets the database by dropping all questions and answers.
     * Preserves reference data (fields, topics, levels, question types).
     * 
     * @return InitializationResult containing counts of deleted entities
     */
    InitializationResult resetDatabase();
    
    /**
     * Initializes or verifies reference data in the database.
     * Creates fields, topics, levels, and question types if they don't exist.
     * 
     * @return InitializationResult containing counts of created entities
     */
    InitializationResult initializeReferenceData();
    
    /**
     * Verifies that all reference data exists and has proper relationships.
     * Checks referential integrity between topics and fields.
     * 
     * @return true if all reference data is valid, false otherwise
     */
    boolean verifyReferenceData();
}
