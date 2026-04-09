# 🧩 Service Overview

* Name: `dbshield-auth-service`
* High-level purpose: Central authentication and user-management service for the DB Shield ecosystem, responsible for issuing access tokens and managing user identities.
* Key responsibilities:
* Authenticate users and issue JWT access tokens.
* Issue and track refresh tokens for session continuity.
* Manage user accounts (create, read, update) with role-based access.
* Maintain audit and security-relevant user attributes (lock status, failed attempts, last login).
* Problem it solves: Provides a single, consistent source of truth for identity and session management so other services do not need to handle credentials directly.

# 🏗 Architecture

* Architectural style: Layered Spring Boot service with clear controller, service, repository, and mapping layers.
* Main modules and responsibilities:
* Controller layer: REST endpoints for authentication and user management under `api/v1/auth` and `api/v1/users`.
* Service layer: Core business logic for login, token refresh, and user CRUD.
* Repository layer: JPA access to `user` and `token` tables.
* DTO / mapping layer: MapStruct mappers isolate API contracts from persistence models.
* Configurations: Spring Security, OpenAPI/Swagger, scheduling, and logging aspect.
* Key design decisions and why they were made:
* JWT access tokens for stateless authorization in downstream services.
* Refresh tokens stored in the database to enable revocation and cleanup.
* Liquibase for deterministic schema evolution.
* MapStruct for reliable, explicit mapping between entities and DTOs.

# 🔄 Business Logic

* Login flow:
* Client submits credentials to `POST /api/v1/auth`.
* Spring Security authenticates via `AuthenticationManager` and `CustomUserDetailsService` against the `user` table.
* Service generates a JWT access token with the user role as a claim.
* Service generates a UUID refresh token and persists it in `token` with expiry.
* Response returns access token and refresh token.
* Refresh flow:
* Client submits refresh token to `POST /api/v1/auth/refresh`.
* Service loads token by UUID, checks `is_revoked` and expiry.
* Service issues a new access token and rotates the refresh token value.
* User management flow:
* `POST /api/v1/users` creates a user, hashes the password with BCrypt, and stores role and status flags.
* `PATCH /api/v1/users` partially updates user fields (nulls ignored).
* `GET /api/v1/users/{id}` and `GET /api/v1/users/us/{username}` return user details.
* Important validations and rules:
* DTO validation enforces username, email, and password constraints.
* Unique constraints on `username` and `email` enforced by the database schema.
* Role-based authorization enforced with `@PreAuthorize` for write operations.
* Scheduled cleanup removes expired refresh tokens.
* Why this logic exists:
* Refresh tokens enable short-lived access tokens without frequent re-authentication.
* Centralized user lifecycle allows consistent role enforcement across services.
* Database persistence of refresh tokens supports revocation and auditability.

# 🔗 Integrations

* Database: PostgreSQL via Spring Data JPA.
* Data sent/received: User credentials, roles, refresh tokens, audit fields.
* When: On login, refresh, and user management operations.
* Why: Durable identity storage and refresh token lifecycle management.
* Migrations: Liquibase initializes and evolves the schema.
* REST interactions:
* Exposes REST endpoints for authentication and user management.
* No outbound REST clients are implemented in the current codebase.
* Message brokers:
* `spring-boot-starter-kafka` is included as a dependency, but no producers or consumers are present yet.
* Authentication / authorization:
* HTTP Basic is enforced for protected endpoints; `/api/v1/auth/*` is public.
* JWTs are generated for downstream validation, but this service itself does not validate JWTs on incoming requests.

# 🗄 Data Model

* `user` table (UserEntity):
* `username` and `email` are unique identifiers; `password_hash` stores BCrypt hashes.
* `role` drives authorization (`READ_WRITE` or `READ_ONLY`).
* `is_locked` and `failed_attempts` are stored for lockout logic (not currently enforced in authentication).
* Audit fields: `created_at`, `updated_at`, and `last_login_at`.
* `token` table (TokenEntity):
* `refresh_token` is a UUID used for session continuation.
* `expire_time` and `is_revoked` control refresh token validity.
* `user_id` links token ownership to the user.
* Relationships:
* One user can have multiple refresh tokens (`user` → `token` one-to-many).
* Schema design rationale:
* Refresh tokens are stored to allow explicit revocation and timed cleanup.
* Unique constraints prevent duplicate identities and tokens.
* Trade-offs:
* Tracking tokens in the database enables control and auditability but adds DB load and cleanup requirements.

# ⚙️ Configuration

* `spring.application.name`: `dbshield-auth-service`.
* Database settings in `application-local.yaml`:
* `spring.datasource.*` for PostgreSQL connection and schema (`auth-service`).
* JPA settings with `ddl-auto: none` and `open-in-view: false`.
* Liquibase:
* `spring.liquibase.change-log` points to `db/changelog/db.changelog-master.yaml`.
* JWT:
* `jwt.secret-key`: shared signing key for JWTs.
* `jwt.expire-time-access-token`: access token lifetime in milliseconds.
* `jwt.expire-time-refresh-token`: refresh token lifetime in hours.
* Scheduling:
* `scheduling.fixed-rate`: refresh token cleanup interval in milliseconds.
* Feature toggles: None in current codebase.

# 🚀 How It Works End-to-End

* Scenario: A user signs into the platform and accesses protected services.
* Client calls `POST /api/v1/auth` with credentials.
* Auth service authenticates against PostgreSQL and returns a JWT access token and refresh token.
* Client uses the JWT to call other services that validate the token using the shared secret.
* When the access token expires, client calls `POST /api/v1/auth/refresh` to rotate tokens.
* Scheduled cleanup purges expired refresh tokens to keep the token store small.

# 📈 Scalability & Performance Considerations

* Database load:
* Login and refresh operations write to the `token` table; high login volumes increase write pressure.
* Cleanup deletes by `expire_time`; consider adding an index on `expire_time` for large token tables.
* Stateless access tokens:
* JWTs reduce auth round-trips for downstream services but increase responsibility for secure key management.
* Bottlenecks:
* Authentication relies on a single user store; consider read replicas if login volume grows.
* Improvements to consider:
* Add persistence for rotated refresh tokens to ensure durability.
* Implement JWT validation in this service if it becomes a gateway or resource server.
* Add metrics for login/refresh rates and cleanup throughput.

# 🔐 Security

* Authentication:
* Username/password via Spring Security `DaoAuthenticationProvider`.
* Passwords hashed with BCrypt before storage.
* Authorization:
* Role-based via `@PreAuthorize` with roles `READ_WRITE` and `READ_ONLY`.
* Token security:
* JWTs signed with a shared secret; access token TTL is short.
* Refresh tokens stored as UUIDs with expiration and revocation flags.
* Sensitive data handling:
* Passwords are never returned; only hashes are stored.
* Note:
* `is_locked` and `failed_attempts` are stored but not enforced in the current authentication flow.

# 🧪 Testing

* Unit tests with Mockito for:
* `AuthServiceImpl` login and refresh flows, including failure cases.
* `UserServiceImpl` CRUD behavior and error paths.
* MapStruct mappers for DTO ↔ entity correctness.
* Integration tests:
* None present for controllers, database, or security filters.
* Test strategy:
* Focused on service logic and mapping correctness; infrastructure behavior is not covered.

# 🧠 Design Decisions & Trade-offs

* JWT + refresh tokens:
* Pro: Stateless access tokens for downstream services, controllable session lifecycle.
* Con: Requires secure key management and refresh token persistence.
* HTTP Basic for protected endpoints:
* Pro: Simple setup for internal administration endpoints.
* Con: Not aligned with token-based auth for resource access; may require a JWT filter if used externally.
* Liquibase migrations:
* Pro: Repeatable, versioned schema management.
* Con: Requires careful coordination of change sets across environments.
* MapStruct for mapping:
* Pro: Compile-time mapping guarantees, less runtime reflection.
* Con: Adds build-time complexity and generated code to manage.

# 📦 Role in Microservice Ecosystem

* This service is the identity authority for DB Shield.
* Other services depend on it to issue JWTs and define user roles.
* If it goes down:
* New logins and refresh operations fail.
* User administration becomes unavailable.
* Existing access tokens remain valid until expiry if other services validate JWTs independently.

# 🎤 Presentation Summary (IMPORTANT)

* Central identity service that issues JWTs and manages user accounts.
* Clean layered architecture with controllers, services, repositories, and mappers.
* Refresh tokens are persisted for revocation and controlled session lifecycles.
* Role-based access (`READ_WRITE`, `READ_ONLY`) enforced through Spring Security.
* Liquibase manages PostgreSQL schema for users and tokens.
* Scheduled cleanup prevents token table growth from expired refresh tokens.
* Clear path for scaling: add token indexes, metrics, and JWT validation where needed.
