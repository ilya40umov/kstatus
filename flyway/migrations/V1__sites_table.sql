CREATE TABLE IF NOT EXISTS sites
(
    site_id                   INT           NOT NULL AUTO_INCREMENT,
    url                       VARCHAR(256)  NOT NULL,
    created_at                DATETIME      NOT NULL,
    check_interval_seconds    INT           NOT NULL,
    last_checked_at           DATETIME      NULL,
    last_status_check_result  VARCHAR(16)   NULL,
    next_scheduled_for        DATETIME      NULL,
    last_enqueued_at          DATETIME      NULL,
    PRIMARY KEY (site_id),
    INDEX (next_scheduled_for)
)
