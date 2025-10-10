-- Ora Android App Database Schema
-- Generated for Room Database
-- Version: 1.0
-- Date: 2025-09-28

-- Users table
CREATE TABLE users (
    id TEXT PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    email TEXT,
    preferredTimeSlot TEXT NOT NULL DEFAULT 'MORNING',
    experienceLevel TEXT NOT NULL DEFAULT 'BEGINNER',
    goals TEXT NOT NULL DEFAULT '[]',
    createdAt TEXT NOT NULL,
    lastActiveAt TEXT NOT NULL,
    isOnboardingCompleted INTEGER NOT NULL DEFAULT 0,
    notificationsEnabled INTEGER NOT NULL DEFAULT 1,
    darkModeEnabled INTEGER NOT NULL DEFAULT 0
);

-- Journal entries table
CREATE TABLE journal_entries (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    date TEXT NOT NULL,
    gratitude1 TEXT NOT NULL,
    gratitude2 TEXT NOT NULL,
    gratitude3 TEXT NOT NULL,
    mood TEXT NOT NULL,
    dayStory TEXT,
    createdAt TEXT NOT NULL,
    updatedAt TEXT NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

-- Content table
CREATE TABLE content (
    id TEXT PRIMARY KEY NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    type TEXT NOT NULL,
    category TEXT NOT NULL,
    durationMinutes INTEGER NOT NULL,
    level TEXT NOT NULL,
    videoUrl TEXT,
    audioUrl TEXT,
    thumbnailUrl TEXT,
    instructorName TEXT,
    tags TEXT NOT NULL DEFAULT '[]',
    isFlashSession INTEGER NOT NULL DEFAULT 0,
    equipment TEXT NOT NULL DEFAULT '[]',
    benefits TEXT NOT NULL DEFAULT '[]',
    createdAt TEXT NOT NULL,
    isOfflineAvailable INTEGER NOT NULL DEFAULT 0,
    downloadSize INTEGER
);

-- User activities table
CREATE TABLE user_activities (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    contentId TEXT NOT NULL,
    sessionType TEXT NOT NULL,
    startedAt TEXT NOT NULL,
    completedAt TEXT,
    durationMinutes INTEGER,
    isCompleted INTEGER NOT NULL DEFAULT 0,
    rating INTEGER,
    notes TEXT,
    streak INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contentId) REFERENCES content(id) ON DELETE CASCADE
);

-- User favorites table
CREATE TABLE user_favorites (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    contentId TEXT NOT NULL,
    addedAt TEXT NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (contentId) REFERENCES content(id) ON DELETE CASCADE,
    UNIQUE(userId, contentId)
);

-- User stats table
CREATE TABLE user_stats (
    userId TEXT PRIMARY KEY NOT NULL,
    totalSessionsCompleted INTEGER NOT NULL DEFAULT 0,
    totalMinutesSpent INTEGER NOT NULL DEFAULT 0,
    currentStreak INTEGER NOT NULL DEFAULT 0,
    longestStreak INTEGER NOT NULL DEFAULT 0,
    lastSessionDate TEXT,
    favoriteContentType TEXT,
    favoriteTimeSlot TEXT,
    updatedAt TEXT NOT NULL,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

-- App settings table
CREATE TABLE app_settings (
    userId TEXT PRIMARY KEY NOT NULL,
    notificationsEnabled INTEGER NOT NULL DEFAULT 1,
    morningReminderTime TEXT,
    dayReminderTime TEXT,
    eveningReminderTime TEXT,
    darkModeEnabled INTEGER NOT NULL DEFAULT 0,
    autoPlayEnabled INTEGER NOT NULL DEFAULT 1,
    downloadOnWifiOnly INTEGER NOT NULL DEFAULT 1,
    keepScreenOn INTEGER NOT NULL DEFAULT 0,
    defaultSessionDuration INTEGER NOT NULL DEFAULT 15,
    language TEXT NOT NULL DEFAULT 'fr',
    biometricAuthEnabled INTEGER NOT NULL DEFAULT 0,
    analyticsEnabled INTEGER NOT NULL DEFAULT 1,
    soundEffectsEnabled INTEGER NOT NULL DEFAULT 1,
    vibrationEnabled INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

-- Notification preferences table
CREATE TABLE notification_preferences (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT NOT NULL,
    type TEXT NOT NULL,
    isEnabled INTEGER NOT NULL DEFAULT 1,
    time TEXT,
    frequency TEXT NOT NULL DEFAULT 'DAILY',
    customMessage TEXT,
    FOREIGN KEY (userId) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for better performance
CREATE INDEX idx_journal_entries_user_date ON journal_entries(userId, date);
CREATE INDEX idx_user_activities_user_id ON user_activities(userId);
CREATE INDEX idx_user_activities_content_id ON user_activities(contentId);
CREATE INDEX idx_user_activities_completed ON user_activities(userId, isCompleted);
CREATE INDEX idx_content_type ON content(type);
CREATE INDEX idx_content_category ON content(category);
CREATE INDEX idx_content_level ON content(level);
CREATE INDEX idx_content_flash_session ON content(isFlashSession);
CREATE INDEX idx_user_favorites_user_id ON user_favorites(userId);
CREATE INDEX idx_notification_preferences_user_type ON notification_preferences(userId, type);

-- Views for common queries
CREATE VIEW user_activity_summary AS
SELECT
    ua.userId,
    c.title as contentTitle,
    c.type as contentType,
    c.category as contentCategory,
    ua.sessionType,
    ua.startedAt,
    ua.completedAt,
    ua.durationMinutes,
    ua.isCompleted,
    ua.rating
FROM user_activities ua
JOIN content c ON ua.contentId = c.id;

CREATE VIEW user_progress_overview AS
SELECT
    u.id as userId,
    u.name as userName,
    us.totalSessionsCompleted,
    us.totalMinutesSpent,
    us.currentStreak,
    us.longestStreak,
    us.lastSessionDate,
    COUNT(DISTINCT uf.contentId) as favoritesCount
FROM users u
LEFT JOIN user_stats us ON u.id = us.userId
LEFT JOIN user_favorites uf ON u.id = uf.userId
GROUP BY u.id;

-- Triggers for data consistency
CREATE TRIGGER update_user_last_active
AFTER INSERT ON user_activities
BEGIN
    UPDATE users
    SET lastActiveAt = NEW.startedAt
    WHERE id = NEW.userId;
END;

CREATE TRIGGER update_user_stats_on_completion
AFTER UPDATE ON user_activities
WHEN NEW.isCompleted = 1 AND OLD.isCompleted = 0
BEGIN
    INSERT OR REPLACE INTO user_stats (
        userId,
        totalSessionsCompleted,
        totalMinutesSpent,
        updatedAt
    )
    VALUES (
        NEW.userId,
        COALESCE((SELECT totalSessionsCompleted FROM user_stats WHERE userId = NEW.userId), 0) + 1,
        COALESCE((SELECT totalMinutesSpent FROM user_stats WHERE userId = NEW.userId), 0) + COALESCE(NEW.durationMinutes, 0),
        datetime('now')
    );
END;

-- Sample data for testing
INSERT INTO users (id, name, email, createdAt, lastActiveAt) VALUES
('user_001', 'Test User', 'test@ora.com', datetime('now'), datetime('now'));

INSERT INTO content (id, title, description, type, category, durationMinutes, level, instructorName, createdAt) VALUES
('content_001', 'Salutation au Soleil', 'Séquence de yoga matinale', 'YOGA', 'MORNING_ROUTINE', 15, 'BEGINNER', 'Sophie Martin', datetime('now')),
('content_002', 'Respiration 4-7-8', 'Technique de respiration relaxante', 'BREATHING', 'STRESS_RELIEF', 5, 'BEGINNER', 'Marc Dubois', datetime('now')),
('content_003', 'Méditation du Soir', 'Méditation pour un sommeil paisible', 'MEDITATION', 'EVENING_WIND_DOWN', 20, 'BEGINNER', 'Claire Petit', datetime('now'));