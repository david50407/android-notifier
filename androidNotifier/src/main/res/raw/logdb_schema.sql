CREATE TABLE IF NOT EXISTS events (
    _id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    source_device_id INTEGER,
    timestamp INTEGER NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    event_type TINYINT NOT NULL,
    payload BLOB);

CREATE INDEX IF NOT EXISTS events_unprocessed ON events (processed, _id);