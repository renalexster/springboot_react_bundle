# GraalVM Native Image Setup Guide

## Overview
This project supports GraalVM Native Image compilation for dramatically reduced memory footprint and faster startup times - ideal for Raspberry Pi deployment.

## Memory Benefits
- **JVM Mode**: 300-500MB RAM, 5-10s startup
- **Native Mode**: 50-150MB RAM, <100ms startup
- **Reduction**: 3-5x less memory, 50-100x faster startup

## Quick Start

### Option 1: Docker Build (Recommended)
```bash
# Build and run with native image
docker-compose -f docker-compose.native.yml up --build

# The backend will use only 128-256MB RAM instead of 512MB+
```

### Option 2: Local Build
```bash
# Prerequisites: Install GraalVM 25+ with native-image tool
# Download from: https://www.graalvm.org/downloads/

# Build native executable
./gradlew :backend:nativeCompile

# Run the native executable
./backend/build/native/nativeCompile/mybootproject
```

## Configuration Files

### 1. backend/build.gradle.kts
Added GraalVM Native Build Tools plugin with optimizations:
```kotlin
plugins {
    id("org.graalvm.buildtools.native") version "0.10.4"
}

graalvmNative {
    binaries {
        named("main") {
            imageName.set("mybootproject")
            buildArgs.add("--gc=G1")  // G1 GC for memory efficiency
            buildArgs.add("-Ob")       // Quick build optimization
        }
    }
}
```

### 2. backend/Dockerfile.native
Multi-stage Docker build using GraalVM:
- **Build Stage**: Uses `ghcr.io/graalvm/native-image-community:25-ol9`
- **Runtime Stage**: Minimal base image with native executable
- **No JVM Required**: Self-contained binary with all dependencies

### 3. docker-compose.native.yml
Docker Compose configuration with memory limits:
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 256M  # vs 512MB+ for JVM
```

### 4. NativeHints.java
Runtime hints for reflection and serialization:
```java
@Configuration
@ImportRuntimeHints(NativeHints.Registrar.class)
public class NativeHints {
    // Registers DTOs and classes needing reflection
}
```

### 5. resource-config.json
Configuration for resources to include in native image:
- application.properties
- Flyway migrations
- YAML configs

## Build Process

### Local Native Build
```bash
# Clean build
./gradlew clean

# Compile native image (takes 5-10 minutes)
./gradlew :backend:nativeCompile

# Output location
ls -lh backend/build/native/nativeCompile/mybootproject
```

### Docker Native Build
```bash
# Set environment variables
export TARGET_PLATFORM=linux/arm64/v8
export GOOGLE_OAUTH_CLIENT_ID=your-client-id

# Build images (takes 10-15 minutes)
docker-compose -f docker-compose.native.yml build

# Start services
docker-compose -f docker-compose.native.yml up -d

# Check memory usage
docker stats
```

## Testing Native Image

### 1. Test Locally
```bash
# Build native image
./gradlew :backend:nativeCompile

# Run with environment variables
export GOOGLE_OAUTH_CLIENT_ID=your-client-id
./backend/build/native/nativeCompile/mybootproject
```

### 2. Test in Docker
```bash
# Build and run
docker-compose -f docker-compose.native.yml up

# Check logs
docker-compose -f docker-compose.native.yml logs backend

# Verify memory usage (should be <150MB)
docker stats mybootproject-backend-1
```

### 3. Test API Endpoints
```bash
# Public endpoint (should work without auth)
curl http://localhost/api/public/hello

# Protected endpoint (requires JWT token)
curl -H "Authorization: Bearer YOUR_TOKEN" \
     http://localhost/api/auth/verify
```

## Troubleshooting

### Missing Reflection Configuration
**Symptom**: `ClassNotFoundException` or `NoSuchMethodException` at runtime

**Solution**: Add hints to `NativeHints.java`:
```java
hints.reflection()
    .registerType(YourClass.class, hint -> hint
        .withMembers(MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                     MemberCategory.INVOKE_PUBLIC_METHODS));
```

### Resource Not Found
**Symptom**: `application.properties` or other resources not found

**Solution**: Update `resource-config.json`:
```json
{
  "resources": {
    "includes": [
      {"pattern": "your-resource-pattern\\.ext"}
    ]
  }
}
```

### Google API Client Issues
**Symptom**: Errors with Google OAuth verification

**Solution**: Uncomment Google-specific hints in `NativeHints.java`:
```java
hints.reflection()
    .registerType(com.google.api.client.json.JsonFactory.class,
        hint -> hint.withMembers(MemberCategory.INVOKE_PUBLIC_METHODS));
```

### Out of Memory During Build
**Symptom**: Native image build fails with OOM

**Solution**: Increase Docker memory or add build arg:
```dockerfile
RUN ./gradlew :backend:nativeCompile \
    -Dorg.gradle.jvmargs="-Xmx4g" \
    --no-daemon
```

### Slow Build Times
**Symptom**: Native build takes too long

**Solution**: Already configured with `-Ob` (quick build). For faster iteration:
- Use JVM mode for development (`docker-compose.yml`)
- Use native mode only for production builds
- Enable build cache (Docker layer caching)

## Comparison: JVM vs Native

### Development
```bash
# JVM mode (faster builds, easier debugging)
docker-compose up --build                    # ~2 min build
docker stats                                 # ~400MB RAM
```

### Production
```bash
# Native mode (memory-efficient, faster startup)
docker-compose -f docker-compose.native.yml up --build  # ~10 min build
docker stats                                            # ~100MB RAM
```

## Raspberry Pi Deployment

### For 64-bit Pi OS
```bash
export TARGET_PLATFORM=linux/arm64/v8
docker-compose -f docker-compose.native.yml up -d
```

### For 32-bit Pi OS
```bash
export TARGET_PLATFORM=linux/arm/v7
docker-compose -f docker-compose.native.yml up -d
```

### Expected Performance
- **Memory**: 80-120MB (backend) + 20-40MB (nginx) = ~150MB total
- **Startup**: <100ms (backend ready in under a second)
- **CPU**: Lower peak usage during startup compared to JVM

## Monitoring Native Image

### Check Memory Usage
```bash
# Container stats
docker stats

# Process memory (inside container)
docker exec mybootproject-backend-1 ps aux
```

### Check Startup Time
```bash
# View logs with timestamps
docker logs mybootproject-backend-1 --timestamps

# Should see application startup in <100ms
```

### Enable JFR (Java Flight Recorder)
Native image includes JFR support:
```bash
# Start with JFR enabled
docker run -e JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=/tmp/recording.jfr" \
    mybootproject-backend-native:local

# Extract recording
docker cp container-id:/tmp/recording.jfr ./
```

## Advanced Configuration

### Custom Memory Limits
Edit `docker-compose.native.yml`:
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 128M  # Minimum for this app
        reservations:
          memory: 64M
```

### Build Args for Different Platforms
Edit `backend/Dockerfile.native`:
```dockerfile
# Add platform-specific optimizations
RUN ./gradlew :backend:nativeCompile \
    -Dgraalvm.native.march=armv8-a \  # For ARM64
    --no-daemon
```

### Enable Additional Monitoring
Edit `backend/build.gradle.kts`:
```kotlin
buildArgs.add("--enable-monitoring=heapdump,jfr,jvmstat,jmxserver")
```

## When to Use Native Image

### ✅ Use Native Mode When:
- Running on Raspberry Pi or resource-constrained devices
- Fast startup time is critical
- Memory usage must be minimized
- Running in production with predictable workloads

### ❌ Use JVM Mode When:
- Active development (faster build times)
- Debugging complex issues
- Using libraries without native support
- Profiling application performance

## Further Reading
- [Spring Boot Native Image Docs](https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html)
- [GraalVM Native Build Tools](https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html)
- [GraalVM Native Image Compatibility](https://www.graalvm.org/latest/reference-manual/native-image/metadata/Compatibility/)
