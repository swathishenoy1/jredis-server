# Yet Another Redis (Java)

Learning project for building a Redis-compatible server in Java, step by step, based on the Codecrafters Redis challenge:
[Build Your Own Redis](https://app.codecrafters.io/courses/redis/overview)

## Current progress

- [x] Step 1: Bind to port `6379` and accept TCP connections
- [x] Step 2: Implement `PING` -> `PONG`
- [x] Step 3: Handle multiple clients concurrently
- [x] Step 4: Parse command-line args (`--port`)
- [x] Step 5: Implement `ECHO`
- [x] Step 6: Implement `SET` and `GET`
- [ ] Next: Implement key expiry (`PX`)

## Project structure

```text
src/main/java/Main.java
```

## Requirements

- Java 8+ (JDK)

## Run locally

Compile:

```bash
javac src/main/java/Main.java
```

Run:

```bash
java -cp src/main/java Main
```

Run with custom port:

```bash
java -cp src/main/java Main --port 6380
```

## Notes

- The server accepts RESP array commands and inline commands.
- Implemented command: `PING` (responds with `+PONG\r\n`).
- Implemented command: `ECHO` (responds with a bulk string).
- Implemented commands: `SET` and `GET` (in-memory key-value store).
- Each client connection is handled in its own thread.
- Server port is configurable via `--port`.
