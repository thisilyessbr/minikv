# MiniKV

MiniKV is an educational, in-memory key-value server written in Java 17. It explores how a TCP server changes as connection handling moves from one client at a time to one thread per client and then to a fixed worker pool.

It is a single-process learning project. It does not implement persistence, replication, authentication, clustering, or the complete Redis protocol.

## Implementations

| Class | Connection model | Shared store | Purpose |
| --- | --- | --- | --- |
| `EchoServer` | Single client loop | None | Socket I/O baseline |
| `KVServer` | Single-threaded | `HashMap` | Sequential SET/GET server |
| `ThreadedKVServer` | One thread per client | `ConcurrentHashMap` | Demonstrates concurrent clients and unbounded thread creation |
| `PooledKVServer` | Fixed pool of 10 workers | `ConcurrentHashMap` | Bounds worker creation while keeping shared access thread-safe |
| `StressTest` | 20 client threads | N/A | Writes and verifies 40,000 unique keys |

All servers listen on TCP port `6381`.

## Protocol

Commands are newline-delimited text:

| Command | Example | Response |
| --- | --- | --- |
| SET | `SET language Java` | `OK` |
| GET | `GET language` | `Java` or `(nil)` |
| Invalid input | `DELETE language` | `ERROR: ...` |

Values can contain spaces because the command parser splits each line into at most three parts.

## Build

Requirements: JDK 17 and Maven.

```bash
mvn clean package
```

Maven creates `target/minikv.jar`.

## Run a server

Start the pooled implementation:

```bash
java -cp target/minikv.jar com.minikv.PooledKVServer
```

Replace the class name with `KVServer`, `ThreadedKVServer`, or `EchoServer` to compare connection models.

Connect from another terminal:

```bash
nc localhost 6381
```

Example session:

```text
SET greeting hello world
OK
GET greeting
hello world
GET missing
(nil)
```

## Run the stress test

Keep one server running, then execute:

```bash
java -cp target/minikv.jar com.minikv.StressTest
```

The test starts 20 clients. Each client writes 2,000 unique keys, and a final connection reads every key back. The report counts successful, missing, and corrupted values.

The utility is useful for comparing implementations, but it is not a formal benchmark: it has no warm-up, latency percentiles, or resource measurements.

## Design notes

- Data lives only in memory and is lost when the server stops.
- The pooled and thread-per-client servers use `ConcurrentHashMap` because their handlers access one shared store.
- The fixed pool limits active handlers, while accepted sockets can still wait in the executor's unbounded queue.
- The protocol has no escaping, size limits, expiry, transactions, or access control.

## License

This repository does not currently declare a software license.
