# security-chaind-lib

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](#contributing)
[![GitHub issues](https://img.shields.io/github/issues/damscassiani1994/security-chaind-lib)](https://github.com/damscassiani1994/security-chaind-lib/issues)

Spring Boot auto-configuration library that wires Spring Security, stateless JWT authentication, and multi-database user validation into any Spring Boot application with zero boilerplate.

**This project is open source and welcomes contributions.** See the [Contributing](#contributing) section to get started.

---

## Features

- Plug-and-play Spring Security configuration via autoconfiguration
- Stateless JWT authentication (JJWT 0.12.x)
- Public endpoints declared in `application.yml` — no code changes needed
- **Strategy Pattern** for database selection: one property switches the active implementation
- Built-in support for **MongoDB, PostgreSQL, MySQL, Oracle and SQL Server**
- Configurable user table / collection schema (field names, table name)
- Role-based authorization: roles loaded from the database are mapped to Spring Security `GrantedAuthority`
- `PasswordHandler` bean exposed for encoding and verifying passwords

---

## Requirements

| Tool | Version |
|------|---------|
| Java | 17+ |
| Spring Boot | 3.x |
| Gradle | 7+ |

---

## Installation

Add the library to your project's `build.gradle`:

```groovy
dependencies {
    implementation 'co.com.scl:security-chaind-lib:1.0.0'

    // Add the driver for the database you chose:
    // MongoDB
    implementation 'org.mongodb:mongodb-driver-sync:5.3.1'

    // PostgreSQL
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'org.postgresql:postgresql:42.7.3'

    // MySQL
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'com.mysql:mysql-connector-j:9.1.0'

    // Oracle
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'com.oracle.database.jdbc:ojdbc11:23.7.0.25.01'

    // SQL Server
    implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    runtimeOnly 'com.microsoft.sqlserver:mssql-jdbc:12.8.1.jre11'
}
```

---

## Configuration

### `application.yml`

```yaml
security:
  chaind:
    # Endpoints that bypass authentication (comma-separated or as a list)
    public-endpoints:
      - /api/v1/auth/login
      - /api/v1/auth/register
      - /actuator/health
      - /error

    # Database type: mongodb | postgresql | mysql | oracle | sqlserver
    database-type: postgresql

    jwt:
      # Base64-encoded secret key used to sign and verify tokens
      secret-key: "bXlTdXBlclNlY3JldEtleUZvckpXVFRva2VuMTIzNDU2Nzg="
      # Token expiration in milliseconds (default: 86400000 = 24 h)
      expiration: 86400000

    datasource:
      # For SQL databases: full JDBC URL
      url: "jdbc:postgresql://localhost:5432/mydb"
      # For MongoDB: connection URI (url) and database name (database)
      # url: "mongodb://localhost:27017"
      # database: "mydb"
      username: "dbuser"
      password: "dbpassword"

    user:
      # Table (SQL) or collection (MongoDB) that stores users
      collection-or-table: users
      # Column / field names
      username-field: username
      password-field: password
      is-active-field: is_active
      # Column / field that holds the user's roles (default: roles)
      roles-field: roles
      # Delimiter used to split roles stored as a single string (SQL only, default: ",")
      roles-separator: ","
```

### `application.properties`

```properties
# Endpoints that bypass authentication (comma-separated list)
security.chaind.public-endpoints=/api/v1/auth/login,/api/v1/auth/register,/actuator/health,/error

# Database type: mongodb | postgresql | mysql | oracle | sqlserver
security.chaind.database-type=postgresql

# JWT
security.chaind.jwt.secret-key=bXlTdXBlclNlY3JldEtleUZvckpXVFRva2VuMTIzNDU2Nzg=
# Token expiration in milliseconds (default: 86400000 = 24 h)
security.chaind.jwt.expiration=86400000

# Datasource — SQL databases use a JDBC URL
security.chaind.datasource.url=jdbc:postgresql://localhost:5432/mydb
# MongoDB: use the URI below and set the database name separately
# security.chaind.datasource.url=mongodb://localhost:27017
# security.chaind.datasource.database=mydb
security.chaind.datasource.username=dbuser
security.chaind.datasource.password=dbpassword

# Table (SQL) or collection (MongoDB) that stores users
security.chaind.user.collection-or-table=users
# Column / field names
security.chaind.user.username-field=username
security.chaind.user.password-field=password
security.chaind.user.is-active-field=is_active
# Column / field that holds the user's roles (default: roles)
security.chaind.user.roles-field=roles
# Delimiter used to split roles stored as a single string (SQL only, default: ",")
security.chaind.user.roles-separator=,
```

### Custom field names

If your user table or collection uses different field names, override them under `security.chaind.user`:

```yaml
security:
  chaind:
    database-type: mongodb
    datasource:
      url: "mongodb://localhost:27017"
      database: "mydb"
    user:
      collection-or-table: usuarios
      username-field: email        # default: username
      password-field: clave        # default: password
      is-active-field: habilitado  # default: is_active
```

The equivalent in `application.properties`:

```properties
security.chaind.database-type=mongodb
security.chaind.datasource.url=mongodb://localhost:27017
security.chaind.datasource.database=mydb
security.chaind.user.collection-or-table=usuarios
security.chaind.user.username-field=email
security.chaind.user.password-field=clave
security.chaind.user.is-active-field=habilitado
```

### Nested fields (MongoDB)

If your authentication fields live inside a sub-document, use dot-notation in the field name properties:

```json
{
  "authentication": {
    "username": "john.doe",
    "password": "$2a$10$...",
    "is_active": true
  }
}
```

```yaml
security:
  chaind:
    user:
      collection-or-table: users
      username-field: authentication.username
      password-field: authentication.password
      is-active-field: authentication.is_active
```

Flat fields and nested paths are both supported — the library resolves the path automatically.

---

### Generating a secret key

The `jwt.secret-key` must be a Base64-encoded string of at least 32 bytes. You can generate one with:

```bash
openssl rand -base64 32
```

---

## Expected database schema

The library queries a single table / collection. The default field names can be overridden via `security.chaind.user.*`.

### SQL (example for PostgreSQL)

```sql
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    roles       VARCHAR(255) NOT NULL DEFAULT 'USER'  -- comma-separated, e.g. "ADMIN,USER"
);
```

### MongoDB

```json
{
  "username": "john.doe",
  "password": "$2a$10$...",
  "is_active": true,
  "roles": ["ADMIN", "USER"]
}
```

`roles` can also be stored as a single delimited string in MongoDB (e.g. `"ADMIN,USER"`) — both shapes are resolved automatically.

---

## How it works

```
application.yml
   └─ security.chaind.database-type = postgresql
         │
         ▼
SecurityChainAutoConfiguration
   ├─ activates PostgreSqlUserAuthStrategy  ◄── Strategy Pattern
   ├─ registers JWTTokenProvider
   ├─ registers JWTAuthorizationFilter
   └─ builds SecurityFilterChain
              ├─ public endpoints → permitAll
              └─ any other request → JWT required
```

Every incoming request passes through `JWTAuthorizationFilter`:

1. Extracts the `Bearer` token from the `Authorization` header.
2. Validates the token signature and expiration using `secret-key`.
3. Loads the user via the active database strategy.
4. Sets the `SecurityContext` — Spring Security handles the rest.

---

## Role-based authorization

Roles loaded from `security.chaind.user.roles-field` are converted into Spring Security `GrantedAuthority` instances and attached to the authenticated user. Each role is upper-cased and prefixed with `ROLE_` if it isn't already (e.g. `admin` → `ROLE_ADMIN`), so you can use the standard Spring Security authorization APIs directly:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/reports")
public List<Report> getReports() { ... }
```

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")
    .anyRequest().authenticated()
);
```

If a user has no roles configured, an empty authority set is used and only authentication (not authorization) is enforced.

---

## Exposing the PasswordHandler

The library registers a `PasswordHandler` bean that your application can inject:

```java
@Service
public class UserService {

    private final PasswordHandler passwordHandler;

    public UserService(PasswordHandler passwordHandler) {
        this.passwordHandler = passwordHandler;
    }

    public String hashPassword(String raw) {
        return passwordHandler.generatePassword(raw);
    }

    public boolean check(String raw, String hashed) {
        return passwordHandler.verifyPassword(raw, hashed);
    }
}
```

---

## Overriding the default behaviour

All beans are registered with `@ConditionalOnMissingBean`, so you can override any of them by declaring your own bean.

**Custom JWT provider** — implement `IJWTProvider` and expose it as a bean; `JWTAuthorizationFilter` will use it automatically:

```java
@Bean
public IJWTProvider myJwtProvider() {
    return new MyJwtProvider();
}
```

**Custom security filter chain** — declare your own `SecurityFilterChain` and the library's chain is skipped entirely:

```java
@Bean
public SecurityFilterChain myCustomChain(HttpSecurity http) throws Exception {
    // fully custom configuration
}
```

---

## Database Strategy Reference

| `database-type` | Strategy class | Driver required |
|---|---|---|
| `mongodb` | `MongoUserAuthStrategy` | `org.mongodb:mongodb-driver-sync` |
| `postgresql` | `PostgreSqlUserAuthStrategy` | `org.postgresql:postgresql` |
| `mysql` | `MySqlUserAuthStrategy` | `com.mysql:mysql-connector-j` |
| `oracle` | `OracleUserAuthStrategy` | `com.oracle.database.jdbc:ojdbc11` |
| `sqlserver` | `SqlServerUserAuthStrategy` | `com.microsoft.sqlserver:mssql-jdbc` |

---

## Contributing

This is an open source project and contributions are very welcome — bug reports, feature requests, documentation improvements, and pull requests.

Repository: [github.com/damscassiani1994/security-chaind-lib](https://github.com/damscassiani1994/security-chaind-lib)

**Found a bug or have an idea?** Open an [issue](https://github.com/damscassiani1994/security-chaind-lib/issues).

**Want to submit a change?**

1. Fork the repository.
2. Create a branch for your change: `git checkout -b feature/my-feature`.
3. Make your changes, keeping the existing code style (Lombok, Strategy Pattern for new database support, `@ConditionalOnMissingBean` / `@ConditionalOnClass` for autoconfiguration beans).
4. Build and verify the project compiles: `./gradlew build`.
5. Commit your changes with a clear message and push the branch to your fork.
6. Open a pull request describing the change and the motivation behind it.

Ideas for contributions include: new database strategy implementations, more configuration options, tests, and documentation fixes. If you're proposing a larger change, opening an issue first to discuss the approach is appreciated.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details. Contributions are accepted under the same license.