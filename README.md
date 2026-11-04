You also need the dokobase-library
https://github.com/vp135/dokobase

## Connection recovery

The client uses UTF-8 line-delimited JSON, TCP keepalive, and application heartbeats to detect an unresponsive server. Failed connections are retried with exponential backoff and jitter. While the server process remains running, a reconnect re-registers the player and sends the game's reconnect messages instead of replaying the old server-side output queue.

Queued client actions are retained in memory until written to the socket, but the protocol does not acknowledge individual actions. A disconnect during a write can therefore leave it uncertain whether the server processed that action; queued actions and game state are also lost if their process exits. Durable recovery and exactly-once action handling require persisted state plus message IDs and acknowledgements.
