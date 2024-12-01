CREATE TABLE IF NOT EXISTS genre (
    genre_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_app (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(40) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    login VARCHAR(20) NOT NULL,
    birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS film (
    film_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    like_film INT,
    genre_id INT,
    rating VARCHAR(10) CHECK (rating IN ('G', 'PG', 'PG-13', 'R', 'NC-17')),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
);

CREATE TABLE IF NOT EXISTS friend_user (
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status_friend VARCHAR(20),
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES user_app(user_id),
    FOREIGN KEY (friend_id) REFERENCES user_app(user_id),
    CHECK (status_friend IN ('FRIEND', 'UNCONFIRMED', 'ACCEPTED'))
);

CREATE TABLE IF NOT EXISTS like_user (
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, film_id),
    FOREIGN KEY (user_id) REFERENCES user_app(user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id)
);

CREATE TABLE IF NOT EXISTS film_user (
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, film_id),
    FOREIGN KEY (user_id) REFERENCES user_app(user_id),
    FOREIGN KEY (film_id) REFERENCES film(film_id)
);