-- Initialize database schema for the chat application

-- Users table
CREATE TABLE IF NOT EXISTS users (
    username VARCHAR(50) PRIMARY KEY,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE'
);

-- Combined ChatRooms table
CREATE TABLE IF NOT EXISTS chatrooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    room_type VARCHAR(20) NOT NULL, -- 'GROUP' or 'PRIVATE'
    max_users INT DEFAULT 50 -- For group chats
);

-- UserSession table
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    login_time DATETIME NOT NULL,
    logout_time DATETIME DEFAULT NULL,
    ip_address VARCHAR(45) NOT NULL,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    sender_username VARCHAR(50) NOT NULL,
    chatroom_id INT NOT NULL,
    timestamp DATETIME NOT NULL,
    FOREIGN KEY (sender_username) REFERENCES users(username),
    FOREIGN KEY (chatroom_id) REFERENCES chatrooms(id) ON DELETE CASCADE
);

-- Chat participants
CREATE TABLE IF NOT EXISTS chat_participants (
    chatroom_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    permission VARCHAR(20) DEFAULT 'MEMBER', -- For group chats: ADMIN, MODERATOR, MEMBER
    PRIMARY KEY (chatroom_id, username),
    FOREIGN KEY (chatroom_id) REFERENCES chatrooms(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);

-- Message delivery status
CREATE TABLE IF NOT EXISTS message_delivery_status (
    message_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SENT', -- SENT, RECEIVED, READ
    PRIMARY KEY (message_id, username),
    FOREIGN KEY (message_id) REFERENCES messages(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
);
