# MiniKV - Java Key-Value Server

**MiniKV** is a collection of Java implementations for a distributed Key-Value (KV) server, demonstrating different approaches to concurrent connection handling.

## 🚀 What is This Project?

This project explores various networking and concurrency patterns in Java by implementing multiple versions of a simple Key-Value store server that listens on port **6381**. The core functionality allows clients to:
- `SET <key> <value>` - Store a value for a given key
- `GET <key>` - Retrieve the value stored under a key (returns "NULL" or "(nil)" if not found)

## 📦 Project Structure

```
src/main/java/com/minikv/
├── EchoServer.java      # Basic echo server (baseline implementation)
├── KVServer.java        # Single-threaded KV server with HashMap storage
├── ThreadedKVServer.java # Creates new thread per client connection
├── PooledKVServer.java  # Uses fixed thread pool for connections
└── StressTest.java      # Load testing utility
```

## 🔍 Implementation Overview

### 1. EchoServer.java
A simple echo server that demonstrates basic socket I/O handling. Every received message is echoed back to the client.

### 2. KVServer.java
The basic Key-Value implementation using a `HashMap` for storage. **Limitation:** Single-threaded - only one client can connect at a time.

### 3. ThreadedKVServer.java
Improves concurrency by creating a new thread (`Thread`) for each incoming client connection. Allows multiple simultaneous clients but creates a new thread per connection (can lead to resource exhaustion under high load).

### 4. PooledKVServer.java
The production-ready implementation using `ExecutorService` with a fixed thread pool (10 threads). Submitting tasks to the pool avoids creating excessive threads and provides better resource management.

### 5. StressTest.java
A stress testing utility to benchmark server performance under load by sending concurrent requests.

## 📋 Protocol Format

Clients connect via TCP to port **6381** and send commands:

| Command | Format        | Response     |
|---------|---------------|--------------|
| SET     | `SET key value` | `OK`         |
| GET     | `GET key`      | `value` or `(nil)`/`NULL` |
| Invalid | Any other     | `ERROR: ...` |

## 🏃‍♂️ How to Run

### Build the Project
```bash
mvn clean package
```

### Run Individual Servers

**EchoServer:**
```bash
java -cp target/minikv-1.0-SNAPSHOT.jar com.minikv.EchoServer
```

**Basic KVServer:**
```bash
java -cp target/minikv-1.0-SNAPSHOT.jar com.minikv.KVServer
```

**Threaded KVServer:**
```bash
java -cp target/minikv-1.0-SNAPSHOT.jar com.minikv.ThreadedKVServer
```

**Pooled KVServer (Recommended):**
```bash
java -cp target/minikv-1.0-SNAPSHOT.jar com.minikv.PooledKVServer
```

### Using a Client

You can use `telnet` or `nc` to connect:

```bash
# Connect to the server
telnet localhost 6381

# Or using netcat
nc localhost 6381
```

Example session:
```
SET mykey hello world
OK
GET mykey
hello world
```

## 🧪 Benchmarking with StressTest

The included [`StressTest.java`](src/main/java/com/minikv/StressTest.java) utility benchmarks server performance under load by running a coordinated write-then-verify test:

### Test Configuration
- **Threads:** 20 concurrent client threads
- **Operations per thread:** 2000 `SET` commands
- **Total operations:** ~40,000 writes (20 threads × 2000 ops)

### How It Works
1. All 20 threads start simultaneously and execute their SET operations
2. After all writes complete, a verification phase reads back every key
3. The test reports: OK count, MISSING keys, and CORRUPTED data

### Running the Stress Test
```bash
# Build the project first
mvn clean package

# Run the stress test
java -cp target/minikv-1.0-SNAPSHOT.jar com.minikv.StressTest
```

**Expected output:**
```
Firing 20 threads x 2000 SETs...
All writes done. Verifying...
Total keys written: 40000
OK:        40000
MISSING:   0
CORRUPTED: 0
```

### Evaluating Server Implementations
- **PooledKVServer** - Should complete in ~1 second with zero missing/corrupted data
- **ThreadedKVServer** - May complete slower due to thread creation overhead (~2-3 seconds)
- **KVServer (single-threaded)** - Will be very slow as only one client can execute at a time

### Interpreting Results
| Result | Meaning |
|--------|---------|
| `OK: 40000` | All operations succeeded |
| `MISSING: X` | Concurrent writes lost data (likely due to non-thread-safe HashMap in basic implementations) |
| `CORRUPTED: X` | Some keys stored with wrong values |

This test demonstrates the importance of proper thread safety and concurrent access handling in KV server implementations.

## 🔑 Key Learnings

| Implementation              | Thread Model      | Best For                     |
|----------------------------|-------------------|------------------------------|
| EchoServer                 | Single thread     | Learning socket basics       |
| KVServer                   | Single thread     | Simple sequential access     |
| ThreadedKVServer           | New thread per client | Low-to-medium concurrent clients |
| PooledKVServer             | Fixed thread pool | Production, high concurrency  |

## 📝 License

This project is educational and open for learning purposes.