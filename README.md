# Yet Another Redis (Java)

Learning project for building a Redis-compatible server in Java, step by step, based on the Codecrafters Redis challenge:
[Build Your Own Redis](https://app.codecrafters.io/courses/redis/overview)

## Current progress

- [x] Step 1: Bind to port `6379` and accept TCP connections
- [ ] Next: Implement `PING` -> `PONG`

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

## Notes

- The server currently accepts connections and closes them immediately.
- Redis protocol handling is not implemented yet (that will be done in the next steps).
