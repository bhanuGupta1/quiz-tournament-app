-- Quiz Results table for storing completed quiz data
-- This table will be auto-created by JPA, but this script can be used for manual setup

CREATE TABLE IF NOT EXISTS quiz_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tournament_id BIGINT NOT NULL,
    score INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    percentage DOUBLE NOT NULL,
    passed BOOLEAN NOT NULL,
    completed_at TIMESTAMP NOT NULL,
    time_taken_seconds INTEGER,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    
    -- Ensure one result per user per tournament
    UNIQUE KEY unique_user_tournament (user_id, tournament_id),
    
    -- Indexes for performance
    INDEX idx_tournament_percentage (tournament_id, percentage DESC),
    INDEX idx_user_completed (user_id, completed_at DESC),
    INDEX idx_tournament_completed (tournament_id, completed_at ASC)
);

-- Sample query to get tournament leaderboard
-- SELECT qr.*, u.first_name, u.last_name, u.email 
-- FROM quiz_results qr 
-- JOIN users u ON qr.user_id = u.id 
-- WHERE qr.tournament_id = ? 
-- ORDER BY qr.percentage DESC, qr.completed_at ASC;