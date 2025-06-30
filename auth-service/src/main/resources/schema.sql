CREATE TABLE user_info (
    id BIGINT AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    whatsapp VARCHAR(255),
    PRIMARY KEY(id)
);