CREATE TABLE IF NOT EXISTS app_user
(
    id       INT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name     VARCHAR(255),
    surname  VARCHAR(255),
    created  DATE NOT NULL,
    updated  DATE NOT NULL,
    status   VARCHAR(255)
);


CREATE TABLE IF NOT EXISTS app_role
(
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(255) UNIQUE NOT NULL,
    created DATE,
    updated DATE
);

CREATE TABLE IF NOT EXISTS app_user_role
(
    app_user_id INT,
    role_id       INT,
    PRIMARY KEY (app_user_id, role_id),
    FOREIGN KEY (app_user_id) REFERENCES app_user (id),
    FOREIGN KEY (role_id) REFERENCES app_role (id)
);