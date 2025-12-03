CREATE TABLE base.email (
    id BIGSERIAL PRIMARY KEY,
    first_name varchar(256) NOT NULL,
    last_name varchar(256) NOT NULL,
    email varchar(512) NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL
);

CREATE TABLE base.email_history (
    id BIGSERIAL PRIMARY KEY,
    send_time TIMESTAMP NOT NULL,
    subject varchar(512) NOT NULL,
    content TEXT NOT NULL,
    email_id BIGINT NOT NULL,
    created_date TIMESTAMP NOT NULL,
    updated_date TIMESTAMP NOT NULL,
    created_by VARCHAR(256) NOT NULL,
    updated_by VARCHAR(256) NOT NULL,
    CONSTRAINT fk_email_history_email
        FOREIGN KEY (email_id) REFERENCES base.email(id)
);