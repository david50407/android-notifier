CREATE TABLE IF NOT EXISTS events (
    _id INT NOT NULL PRIMARY KEY AUTOINCREMENT,
    source_device_id INT,
    timestamp INT NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    event_type TINYINT NOT NULL,
    payload BLOB);

CREATE INDEX IF NOT EXISTS events_unprocessed ON events (processed, _id);
