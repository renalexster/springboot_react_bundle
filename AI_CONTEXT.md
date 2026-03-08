# AI Context Documentation - myBootProject

## Project Overview
A full-stack web application with Spring Boot backend and React frontend, implementing Google OAuth 2.0 authentication. The project is designed to run on Raspberry Pi 4 (64-bit) using Docker containers with multi-architecture support.

## Tech Stack

### Backend
- **Framework**: Spring Boot 4.0.2 (Spring 7)
- **Language**: Java 25
- **Build Tool**: Gradle 8.7+ (Kotlin DSL)
- **Security**: Spring Security with OAuth2 Resource Server
- **Authentication**: Google OAuth 2.0 (ID Token verification)
- **Database**: H2 (embedded, with console)
- **Migration**: Flyway
- **Data Access**: Spring Data JDBC
- **Circuit Breaker**: Resilience4j (Spring Cloud Circuit Breaker)
- **Dependencies**:
  - `google-api-client` for Google ID token verification
  - `google-http-client-jackson2` for JSON processing
- **Runtime**: Eclipse Temurin JRE 17 (JVM mode) or GraalVM Native Image (native mode)
- **GraalVM Support**: Native Image compilation for reduced memory footprint and faster startup

### Frontend
- **Framework**: React 18.3.1
- **Language**: TypeScript 5.6.3
- **Build Tool**: Vite 5.1.0
- **Routing**: Hash-based routing (custom hook `useHashLocation`)
- **Styling**: React components (minimal external dependencies)
- **Runtime**: Nginx 1.27 (Alpine)

### Infrastructure
- **Containerization**: Docker (multi-stage builds)
- **Orchestration**: Docker Compose 3.9
- **Target Platform**: `linux/arm64/v8` (Raspberry Pi 4 64-bit OS)
- **Alternative Platform**: `linux/arm/v7` (32-bit Pi OS)
- **Proxy**: Nginx (reverse proxy for backend API)

## Project Structure

```
myBootProject/
├── backend/                    # Spring Boot backend module
│   ├── src/main/
│   │   ├── java/com/example/service/
│   │   │   ├── MyBootProjectApplication.java    # Main application entry point
│   │   │   ├── auth/
│   │   │   │   ├── AuthController.java          # Protected auth endpoints (/api/auth/*)
│   │   │   │   ├── PublicController.java        # Public endpoints (/api/public/*)
│   │   │   │   └── dto/                         # Data Transfer Objects
│   │   │   │       ├── User.java
│   │   │   │       └── VerifyResponse.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java          # OAuth2 + CORS configuration
│   │   │   │   ├── ExecutionContext.java        # Request context interface
│   │   │   │   ├── ExecutionContextImpl.java    # ThreadLocal context implementation
│   │   │   │   ├── ExecutionContextFilter.java  # Filter to populate context
│   │   │   │   └── RequestContext.java          # Context data (user, traceId)
│   │   │   └── utils/
│   │   │       └── StringUtils.java
│   │   └── resources/
│   │       └── application.properties           # Spring Boot configuration
│   ├── build.gradle.kts                         # Backend dependencies
│   └── Dockerfile                               # Multi-stage Docker build
├── ui/                                          # React frontend module
│   ├── src/
│   │   ├── App.tsx                              # Main app component (router)
│   │   ├── main.tsx                             # React entry point
│   │   ├── hooks/
│   │   │   └── useHashLocation.ts               # Custom hash routing hook
│   │   ├── pages/
│   │   │   ├── Login.tsx                        # Google Sign-In page
│   │   │   └── Welcome.tsx                      # Protected welcome page
│   │   ├── services/
│   │   │   ├── api.ts                           # API fetch wrapper
│   │   │   └── auth.ts                          # Authentication utilities
│   │   └── types/
│   │       └── env.d.ts                         # TypeScript environment types
│   ├── package.json                             # Frontend dependencies
│   ├── vite.config.js                           # Vite configuration
│   ├── tsconfig.json                            # TypeScript configuration
│   ├── nginx.conf                               # Nginx configuration for serving
│   └── Dockerfile                               # Multi-stage Docker build
├── build.gradle.kts                             # Root project configuration
├── settings.gradle.kts                          # Multi-module project setup
└── docker-compose.yml                           # Container orchestration

```

## Architecture Patterns

### 1. Multi-Module Gradle Project
- **Root Project**: Coordinates build for both backend and ui modules
- **Submodules**: `:backend` (Spring Boot), `:ui` (Vite React)
- **Build Isolation**: Each module has its own `build.gradle.kts`

### 2. Multi-Stage Docker Builds
- **Backend**: Gradle build stage → JRE runtime stage (smaller image)
- **Frontend**: Node build stage → Nginx runtime stage (static assets)

### 3. Execution Context Pattern
- **Interface**: `ExecutionContext` provides access to request-scoped data
- **Implementation**: `ExecutionContextImpl` uses ThreadLocal for thread safety
- **Filter**: `ExecutionContextFilter` populates context from JWT and headers
- **Data**: `RequestContext` record holds `user` and `traceId`
- **Usage**: Controllers inject `ExecutionContext` to access current user

### 4. OAuth2 Resource Server Pattern
- **JWT Validation**: Spring Security validates Google ID tokens
- **Custom Validators**: Audience validator checks `google.oauth.client-id`
- **Token Extraction**: JWT automatically extracted from `Authorization: Bearer` header
- **User Mapping**: JWT claims mapped to `User` DTO (name, email, etc.)

### 5. CORS Configuration
- **Allowed Origins**: Configurable via `app.cors.allowed-origin` (default: `http://localhost:5173`)
- **Credentials**: `allowCredentials: true` for cookie-based auth (if needed)
- **Custom Headers**: `x-traceId` for distributed tracing

### 6. API Proxy Pattern
- **Frontend**: Nginx proxies `/api/*` requests to backend container
- **Same-Origin**: Eliminates CORS issues in production
- **Environment Variable**: `VITE_API_BASE_URL=/api` set at build time

### 7. Hash-Based Routing
- **Client-Side**: Uses `window.location.hash` for routing (no server config needed)
- **Custom Hook**: `useHashLocation()` provides reactive hash path
- **Routes**: `#/` → Login, `#/welcome` → Welcome

## Security

### Authentication Flow
1. User clicks "Sign in with Google" on Login page
2. Google Sign-In popup authenticates user and returns ID token
3. Frontend stores token in `localStorage`
4. Frontend sends token in `Authorization: Bearer` header for protected requests
5. Backend validates token signature, issuer, and audience (client ID)
6. Backend extracts user info from JWT claims
7. `ExecutionContextFilter` populates `RequestContext` with user and traceId

### Protected Routes
- **Backend**: `/api/auth/*` requires valid JWT
- **Frontend**: `Welcome` page redirects to Login if no token

### Public Routes
- **Backend**: `/api/public/*` permits all
- **Frontend**: `Login` page accessible to all

## Configuration

### Environment Variables

#### Backend
- `GOOGLE_OAUTH_CLIENT_ID`: Google OAuth 2.0 client ID (required for production)
- `APP_CORS_ALLOWED_ORIGIN`: CORS allowed origin (default: `http://localhost:5173`)
- `SERVER_PORT`: Backend port (default: `8080`)
- `JAVA_OPTS`: JVM tuning for Raspberry Pi (default: `-XX:MaxRAMPercentage=60.0`)

#### Frontend (Build-Time)
- `VITE_API_BASE_URL`: API base URL (default: `/api` for nginx proxy)

#### Docker Compose
- `TARGET_PLATFORM`: Target architecture (default: `linux/arm64/v8`)
- `GOOGLE_OAUTH_CLIENT_ID`: Passed from host environment

### Application Properties
- **Config File**: `backend/src/main/resources/application.properties`
- **Spring App Name**: `myBootProject`
- **Logging Pattern**: Includes `traceId` from MDC after log level

## Build & Deployment

### Local Development
```bash
# Backend (from root)
./gradlew :backend:bootRun

# Frontend (from ui/)
cd ui && npm run dev
```

### Docker Build & Run
```bash
# Set environment variables (optional)
export TARGET_PLATFORM=linux/arm64/v8
export GOOGLE_OAUTH_CLIENT_ID=your-client-id

# Build and start containers
docker-compose up --build

# Access application
# Frontend: http://localhost (port 80)
# Backend API: http://localhost/api (proxied by nginx)
```

### Raspberry Pi Deployment
1. Clone repository to Pi
2. Set `TARGET_PLATFORM=linux/arm64/v8` (64-bit) or `linux/arm/v7` (32-bit)
3. Set `GOOGLE_OAUTH_CLIENT_ID` in `.env` file
4. Run `docker-compose up -d`

## Key Files

### Configuration
- `backend/src/main/resources/application.properties` - Spring Boot config
- `backend/build.gradle.kts` - Backend dependencies and GraalVM native config
- `ui/vite.config.js` - Vite build configuration
- `ui/nginx.conf` - Nginx reverse proxy configuration
- `docker-compose.yml` - Container orchestration (JVM mode)
- `docker-compose.native.yml` - Container orchestration (GraalVM native mode)
- `backend/Dockerfile` - JVM-based Docker build
- `backend/Dockerfile.native` - GraalVM native image Docker build

### Security
- `backend/src/main/java/com/example/service/config/SecurityConfig.java` - OAuth2 + CORS setup
- `backend/src/main/java/com/example/service/config/ExecutionContextFilter.java` - Request context filter
- `backend/src/main/java/com/example/service/config/NativeHints.java` - GraalVM reflection hints

### API Endpoints
- `backend/src/main/java/com/example/service/auth/AuthController.java` - Protected endpoints
- `backend/src/main/java/com/example/service/auth/PublicController.java` - Public endpoints

### Frontend Services
- `ui/src/services/auth.ts` - Token management and verification
- `ui/src/services/api.ts` - API fetch wrapper with auth headers

## Development Notes

### Testing
- Backend tests use Spring Boot test slices (`-test` dependencies)
- Tests excluded from Docker build with `-x test` flag (for speed)
- JUnit Platform Launcher included in test runtime

### Logging
- MDC (Mapped Diagnostic Context) used for `traceId` propagation
- Custom log pattern shows `traceId` after log level
- ANSI colors enabled for local development

### Memory Tuning
- **JVM Mode**: Default JVM heap: 60% of container memory (Pi optimization)
- **JVM Mode**: Configurable via `JAVA_OPTS` environment variable
- **JVM Mode**: UseContainerSupport flag for accurate memory detection
- **Native Mode**: GraalVM Native Image uses 50-150MB RAM vs 300-500MB for JVM
- **Native Mode**: Startup time: <100ms vs 5-10s for JVM

## Common Tasks

### Add New Protected Endpoint
1. Create controller method in `AuthController` or new controller
2. Add `@RequestMapping("/api/auth/your-path")` annotation
3. Inject `ExecutionContext` to access current user
4. SecurityConfig already protects `/api/auth/*` routes

### Add New Public Endpoint
1. Create controller method in `PublicController` or new controller
2. Add `@RequestMapping("/api/public/your-path")` annotation
3. SecurityConfig already permits `/api/public/*` routes

### Change Frontend Route
1. Update `App.tsx` routing logic
2. Add new page component in `ui/src/pages/`
3. Use `window.location.hash` to navigate

### Update Dependencies
- **Backend**: Modify `backend/build.gradle.kts`, run `./gradlew build`
- **Frontend**: Modify `ui/package.json`, run `npm install`

### Build GraalVM Native Image
```bash
# Local build (requires GraalVM installed)
./gradlew :backend:nativeCompile

# Docker build with native image
docker-compose -f docker-compose.native.yml up --build

# Run native executable directly
./backend/build/native/nativeCompile/mybootproject
```

### Switch Between JVM and Native Mode
- **JVM Mode**: Use `docker-compose.yml` with `backend/Dockerfile`
- **Native Mode**: Use `docker-compose.native.yml` with `backend/Dockerfile.native`
- **Memory Limit**: Native mode typically needs only 128-256MB vs 512MB+ for JVM

## CI/CD Considerations
- Multi-stage builds optimize image size
- Build caching via layer ordering (dependencies before source)
- Gradle daemon disabled in Docker for reproducible builds
- Architecture specified via `TARGET_PLATFORM` for cross-compilation

## GraalVM Native Image Benefits

### Memory Footprint Comparison
| Metric | JVM Mode | Native Mode | Improvement |
|--------|----------|-------------|-------------|
| Startup Time | 5-10s | <100ms | 50-100x faster |
| Memory Usage | 300-500MB | 50-150MB | 3-5x reduction |
| Base Image Size | ~200MB (JRE) | ~150MB (native + base) | Smaller |
| Peak Memory | 512MB+ | 128-256MB | 2-4x reduction |

### GraalVM Configuration
- **Plugin**: `org.graalvm.buildtools.native` version 0.10.4
- **Build Task**: `./gradlew :backend:nativeCompile`
- **Output**: `backend/build/native/nativeCompile/mybootproject`
- **GC**: G1 (configured for better memory management)
- **Monitoring**: Enabled for heapdump, JFR, jvmstat
- **Metadata**: Auto-detection enabled + custom hints in `NativeHints.java`

### Native Image Limitations
- No dynamic class loading at runtime
- Reflection requires explicit configuration (handled by Spring Boot + custom hints)
- Some libraries may need additional configuration
- Longer build times (5-10 minutes vs 1-2 minutes for JVM)
- Debugging is more complex (use `-H:+ReportExceptionStackTraces`)

### When to Use Native vs JVM
- **Use Native Mode**: Production on resource-constrained devices (Raspberry Pi), fast startup needed, minimal memory footprint
- **Use JVM Mode**: Development (faster builds), debugging, dynamic features needed

## Links & Resources
- Spring Boot 4.0.2 docs: https://docs.spring.io/spring-boot/index.html
- Spring Boot Native Image: https://docs.spring.io/spring-boot/reference/packaging/native-image/index.html
- GraalVM Native Build Tools: https://graalvm.github.io/native-build-tools/latest/gradle-plugin.html
- Spring Security OAuth2: https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html
- React 18: https://react.dev/
- Vite: https://vitejs.dev/
- Google OAuth 2.0: https://developers.google.com/identity/protocols/oauth2
