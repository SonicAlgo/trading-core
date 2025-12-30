# Trading Core

Core library for trading SDKs providing reusable HTTP and WebSocket client infrastructure.

## Features

- **HTTP Client**: Generic HTTP client (GET, POST, PUT, PATCH, DELETE) with Jackson serialization, rate limit retry with exponential backoff
- **WebSocket Client**: Base WebSocket client with automatic reconnection support
- **Configuration Interfaces**: Clean separation of HTTP and WebSocket configuration
- **Utilities**: TOTP generation, query parameter conversion, file download with GZIP support

## Installation

### Gradle (Kotlin DSL)
```kotlin
implementation("io.github.sonicalgo:trading-core:1.2.0")
```

### Gradle (Groovy)
```groovy
implementation 'io.github.sonicalgo:trading-core:1.2.0'
```

### Maven
```xml
<dependency>
    <groupId>io.github.sonicalgo</groupId>
    <artifactId>trading-core</artifactId>
    <version>1.2.0</version>
</dependency>
```

## Components

### HTTP Client (`io.github.sonicalgo.core.client`)

- `HeaderProvider` - Interface for providing HTTP headers (authentication, etc.)
- `HttpClient` - Generic HTTP client with GET, POST, PUT, PATCH, DELETE support, plus file download methods (`downloadJson`, `downloadGzipJson`, `downloadBytes`, `downloadString`)
- `HttpClientProvider` - Factory for OkHttpClient instances (HTTP, WebSocket, and file download clients)

### Configuration (`io.github.sonicalgo.core.config`)

- `HttpSdkConfig` - HTTP client configuration (timeouts, logging, rate limit retries)
- `WebSocketSdkConfig` - WebSocket configuration (reconnection settings, ping interval)

### WebSocket (`io.github.sonicalgo.core.websocket`)

- `BaseWebSocketClient` - Abstract base class with automatic reconnection
- `ConnectionState` - Enum for WebSocket connection states

### Exception (`io.github.sonicalgo.core.exception`)

- `SdkException` - Base exception with error categorization methods
- `MaxReconnectAttemptsExceededException` - WebSocket reconnection failure

### Utilities (`io.github.sonicalgo.core.util`)

- `TotpUtil` - RFC 6238 TOTP generation with Base32 support
- `toQueryParams()` - Convert objects to query parameter maps

## Requirements

- Java 11+
- Kotlin 2.2.x

## Building

```bash
./gradlew build
```

## License

MIT License
