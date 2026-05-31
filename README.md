# 🐾 Veterinary PetCare Shop — Backend

> Spring Boot 3.5.14 · Java 21 · PostgreSQL · Redis · JWT (RSA) · Flyway

Backend API cho ứng dụng quản lý phòng khám thú y kết hợp shop pet (chó & mèo).
Cung cấp REST API cho web Next.js và mobile app.

---

## 📑 Mục lục

- [Tech Stack](#-tech-stack)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt môi trường](#-cài-đặt-môi-trường)
  - [Java 21](#1-java-21)
  - [PostgreSQL](#2-postgresql-17)
  - [Redis](#3-redis-7)
- [Setup dự án](#-setup-dự-án)
  - [Clone & cấu trúc](#1-clone-repo)
  - [Tạo database](#2-tạo-database-postgresql)
  - [Tạo file .env.properties](#3-tạo-file-envproperties)
  - [Sinh RSA keys](#4-sinh-rsa-key-pair-public--private)
- [Chạy dự án](#-chạy-dự-án)
- [API Endpoints](#-api-endpoints-hiện-có)
- [Cấu trúc thư mục](#-cấu-trúc-thư-mục)
- [Troubleshooting](#-troubleshooting)

---

## 🚀 Tech Stack

| Layer               | Tech                               | Version                    |
| ------------------- | ---------------------------------- | -------------------------- |
| Framework           | Spring Boot                        | 3.5.14                     |
| Language            | Java                               | 21 (LTS)                   |
| Build               | Maven                              | 3.9+                       |
| Database            | PostgreSQL                         | 17                         |
| Cache / Token store | Redis                              | 7+                         |
| Migration           | Flyway                             | auto (Spring Boot managed) |
| Auth                | OAuth2 Resource Server + JWT (RSA) | auto                       |
| ORM                 | Spring Data JPA + Hibernate        | auto                       |
| Mapping             | MapStruct                          | 1.5.5.Final                |
| Password hash       | password4j (BCrypt)                | 1.8.4                      |
| UUID generation     | uuid-creator (UUIDv7)              | 6.0.0                      |
| Boilerplate         | Lombok                             | 1.18.38                    |

---

## 💻 Yêu cầu hệ thống

- **Java JDK 21** (Temurin / Oracle / Amazon Corretto đều OK)
- **Maven 3.9+** (hoặc dùng `./mvnw` wrapper kèm repo)
- **PostgreSQL 17** (port mặc định `5432`)
- **Redis 7+** (port mặc định `6379`)
- **OpenSSL** (để sinh RSA key pair)
- **Git**
- IDE: IntelliJ IDEA / VS Code / Eclipse

---

## ⚙️ Cài đặt môi trường

### 1. Java 21

#### Windows (chocolatey)

```powershell
choco install temurin21
```

#### macOS (homebrew)

```bash
brew install --cask temurin@21
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk
```

#### Verify

```bash
java -version
# openjdk version "21.0.x" ...

javac -version
# javac 21.0.x
```

Set `JAVA_HOME` (Windows PowerShell):

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot", "User")
```

=---

### 2 PostgreSQL 17

#### Windows

Tải installer: https://www.postgresql.org/download/windows/
Set password root: `123456` (khớp với `.env.properties`)#### macOS

```bash
brew install postgresql@17
brew services start postgresql@17
```

#### Linux (Ubuntu)

```bash
sudo apt install -y postgresql-17
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Đổi password user postgres
sudo -u postgres psql
\password postgres
# Nhập: 123456
\q
```

#### Verify

```bash
psql --version
# psql (PostgreSQL) 17.x

# Test kết nối
psql -U postgres -h localhost
```

=---

### 3 Redis 7+

#### Windows

**Cách 1 (khuyên dùng): Docker Desktop**

```bash
docker run -d --name petcare-redis -p 6379:6379 redis:7-alpine redis-server --requirepass 123456
```

**Cách 2: Memurai (Redis-compatible cho Windows)**
https://www.memurai.com/get-memurai

**Cách 3: WSL2 + Linux Redis**

```bash
wsl --install
# Trong WSL Ubuntu:
sudo apt install redis-server
```

#### macOS

```bash
brew install redis
brew services start redis

# Set password
redis-cli
CONFIG SET requirepass "123456"
CONFIG REWRITE
exit
```

#### Linux (Ubuntu)

```bash
sudo apt install -y redis-server
sudo nano /etc/redis/redis.conf
# Uncomment dòng: requirepass 123456
sudo systemctl restart redis-server
```

#### Verify

```bash
redis-cli -h localhost -p 6379 -a 123456 ping
# PONG
```

=---

## 📦 Setup dự án### 1 Clone repo

```bash
git clone <repo-url> graduation_project_be
cd graduation_project_be
```

### 2. Tạo database PostgreSQL

```bash
# Login psql
psql -U postgres -h localhost

# Trong psql shell:
CREATE DATABASE veterinaryshop
    WITH ENCODING 'UTF8'
    LC_COLLATE 'en_US.UTF-8'
    LC_CTYPE 'en_US.UTF-8';

# Verify
\l
# Phải thấy 'veterinaryshop' trong list

\q
```

> ⚠️ **Không cần chạy SQL nào khác**. Flyway sẽ tự apply các file `V1__init.sql`, `V2__catalog.sql`, `V3__product.sql`... khi Spring Boot khởi động lần đầu.

### 3. Tạo file `.env.properties`

Tạo file `.env.properties` ở **root repo** (cùng cấp với `pom.xml`):

```properties
# ===== Database =====
DB_URL=jdbc:postgresql://localhost:5432/veterinaryshop
DB_USERNAME_POSTGRES=postgres
DB_PASSWORD=123456

# ===== Redis =====
REDIS_USERNAME=default
REDIS_PASSWORD=123456
REDIS_HOST=localhost
REDIS_PORT=6379

# ===== CORS (frontend origins được phép gọi API) =====
CORS_ALLOWED_ORIGINS=http://localhost:5275,http://localhost:5279,http://localhost:3000

# ===== JWT expiration (milliseconds) =====
JWT_ACCESS_EXPIRATION=900000
# 17 phút = 17 * 60 * 1000

JWT_REFRESH_EXPIRATION=2592000000
# 30 ngày = 30 * 24 * 60 * 60 * 1000

# ===== RSA Keys (đường dẫn trong classpath) =====
RSA_PUBLIC_KEY=classpath:keys/public.pem
RSA_PRIVATE_KEY=classpath:keys/private.pem
```

> 🔒 **Bảo mật quan trọng:**
>
> - File `.env.properties` **KHÔNG được commit** lên Git.
> - Thêm vào `.gitignore`:
>
>   ```gitignore
>   # Env
>   .env.properties
>   .env
>   .env.local
>
>   # RSA keys (CẤM commit private key)
>   src/main/resources/keys/*.pem
>   ```
>
> - Tạo file `.env.properties.example` (mẫu rỗng) để team biết schema:
>   ```properties
>   DB_URL=jdbc:postgresql://localhost:5432/veterinaryshop
>   DB_USERNAME_POSTGRES=
>   DB_PASSWORD=
>   # ... (các key khác để rỗng)
>   ```

### 4. Sinh RSA key pair (public + private)

JWT trong dự án này dùng **RSA signature** (không phải HMAC), nên cần cặp khoá public/private. Spring Boot sẽ:

- Dùng **private key** để **sign** JWT khi cấp token
- Dùng **public key** để **verify** JWT khi nhận request

#### Bước 1: Tạo thư mục chứa keys

```bash
# Từ root repo
mkdir -p src/main/resources/keys
cd src/main/resources/keys
```

#### Bước 2: Sinh private key (RSA 2048-bit, định dạng PKCS#8)

```bash
# Sinh private key
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048

# Sinh public key tương ứng từ private key
openssl rsa -pubout -in private.pem -out public.pem
```

#### Bước 3: Verify

```bash
# Xem nội dung public key (an toàn, có thể chia sẻ)
cat public.pem
# -----BEGIN PUBLIC KEY-----
# MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...
# -----END PUBLIC KEY-----

# Xem private key (PHẢI giữ bí mật)
cat private.pem
# -----BEGIN PRIVATE KEY-----
# MIIEvQIBADANBgkqhkiG9w0BAQEFAAS...
# -----END PRIVATE KEY-----
```

#### Trên Windows (PowerShell, không có OpenSSL sẵn)

**Cách 1**: Cài OpenSSL qua Chocolatey

```powershell
choco install openssl
# Đóng terminal mở lại, rồi chạy lệnh openssl như trên
```

**Cách 2**: Dùng Git Bash (đã có sẵn openssl)

```bash
# Mở Git Bash trong thư mục src/main/resources/keys
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in private.pem -out public.pem
```

**Cách 3**: WSL2

```bash
wsl
cd /mnt/d/your-repo-path/src/main/resources/keys
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in private.pem -out public.pem
```

#### Cấu trúc sau khi sinh keys

```
src/main/resources/
├── application.properties
├── application-dev.properties
├── db/
│   └── migration/
│       ├── V1__init.sql
│       ├── V2__catalog.sql
│       └── V3__product.sql
└── keys/
    ├── private.pem      ← BÍ MẬT, không commit
    └── public.pem       ← Có thể chia sẻ
```

> ⚠️ **Cảnh báo bảo mật:**
>
> - **TUYỆT ĐỐI KHÔNG** commit `private.pem` lên Git public.
> - Mỗi môi trường (dev/staging/prod) nên có cặp keys **riêng**.
> - Production: lưu private key ở vault (AWS Secrets Manager / HashiCorp Vault), không nhúng vào source.

---

## ▶️ Chạy dự án

### Cách 1: Maven Wrapper (khuyên dùng, không cần cài Maven)

```bash
# Linux/macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Cách 2: Maven cài sẵn

```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

### Cách 3: Build JAR rồi chạy

```bash
mvn clean package -DskipTests
java -jar target/project-0.0.1-SNAPSHOT.jar
```

### Cách 4: IntelliJ IDEA

1. `File > Open` → chọn thư mục repo
2. Đợi Maven import deps
3. Mở `ProjectApplication.java` → Click nút **Run** (biểu tượng tam giác xanh)
4. Hoặc Run Configuration → set **Environment file** trỏ vào `.env.properties`

### Log thành công

Khi app chạy OK, bạn sẽ thấy log dạng:

```
:: Spring Boot ::                (v3.5.14)

INFO  o.f.c.i.database.base.BaseDatabaseType - Database: jdbc:postgresql://localhost:5432/veterinaryshop (PostgreSQL 17.x)
INFO  o.f.core.internal.command.DbMigrate    - Current version of schema "public": << Empty Schema >>
INFO  o.f.core.internal.command.DbMigrate    - Migrating schema "public" to version "1 - init"
INFO  o.f.core.internal.command.DbMigrate    - Migrating schema "public" to version "2 - catalog"
INFO  o.f.core.internal.command.DbMigrate    - Migrating schema "public" to version "3 - product"
INFO  o.f.core.internal.command.DbMigrate    - Successfully applied 3 migrations to schema "public"

INFO  o.s.b.w.embedded.tomcat.TomcatWebServer - Tomcat started on port 8990 (http) with context path '/'
INFO  c.g.project.ProjectApplication         - Started ProjectApplication in 5.234 seconds
```

**App chạy ở:** `http://localhost:8990`

### Test nhanh

```bash
# Health check
curl http://localhost:8990/actuator/health
# {"status":"UP"}

# Get categories
curl http://localhost:8990/api/catalog/categories/tree

# Get products
curl http://localhost:8990/api/products
```

=---

## 📡 API Endpoints hiện có### Public (không cần token)

| Method | Endpoint                         | Description                                    |
| ------ | -------------------------------- | ---------------------------------------------- | ------------------------------------ |
| POST   | `/api/auth/register`             | Đăng ký tài khoản                              |
| POST   | `/api/auth/login`                | Đăng nhập (web - set HttpOnly cookie)          |
| POST   | `/api/auth/login-mobile`         | Đăng nhập (mobile - trả refresh trong body)    |
| POST   | `/api/auth/refresh`              | Refresh access token (web)                     |
| POST   | `/api/auth/refresh-mobile`       | Refresh access token (mobile)                  |
| POST   | `/api/auth/logout`               | Đăng xuất                                      |
| GET    | `/api/catalog/categories/tree`   | Cây danh mục                                   |
| GET    | `/api/catalog/categories`        | Tất cả danh mục                                |
| GET    | `/api/catalog/categories/{slug}` | Chi tiết danh mục                              |
| GET    | `/api/catalog/brands`            | Tất cả thương hiệu                             |
| GET    | `/api/catalog/brands/{slug}`     | Chi tiết thương hiệu                           |
| GET    | `/api/products`                  | List products có filter/search/sort/pagination |
| GET    | `/api/products/{slug}`           | Chi tiết sản phẩm                              |
| GET    | `/api/products/{slug}/related`   | Sản phẩm tương tự                              |
| GET    | `/api/products/featured`         | Sản phẩm nổi bật                               |
| GET    | `/actuator/health`               | Health check                                   | ### Authenticated (cần Bearer token) |

| Method | Endpoint        | Description           |
| ------ | --------------- | --------------------- |
| GET    | `/api/users/me` | Profile user hiện tại |
| PUT    | `/api/users/me` | Update profile        |

> Format response chuẩn: `ApiResp<T> {message,data,timestamp}`

---

## 📂 Cấu trúc thư mục

```
graduation_project_be/
├── src/
│   ├── main/
│   │   ├── java/com/graduation/project/
│   │   │   ├── auth/                    # Authentication module (CỐ ĐỊNH)
│   │   │   │   ├── config/              # Security, JWT, Rate limit
│   │   │   │   ├── controller/          # AuthController, UserController
│   │   │   │   ├── dto/                 # Login/Register DTOs
│   │   │   │   ├── entity/              # Role
│   │   │   │   ├── exception/           # Auth exception handlers
│   │   │   │   ├── keys/                # RSA key properties
│   │   │   │   ├── mapper/              # UserMapper
│   │   │   │   ├── repository/          # RoleRepository
│   │   │   │   ├── service/             # AuthService, UserService
│   │   │   │   └── utils/               # SecurityUtils
│   │   │   ├── catalog/                 # Category & Brand module (Sprint 1.1)
│   │   │   ├── product/                 # Product module (Sprint 1.2)
│   │   │   ├── common/
│   │   │   │   ├── exception/           # GlobalExceptionHandler
│   │   │   │   └── resp/                # ApiResp<T> wrapper
│   │   │   ├── user/                    # User entity & repo
│   │   │   ├── utils/                   # UuidV7Generator
│   │   │   └── ProjectApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── db/migration/            # Flyway SQL
│   │       │   ├── V1__init.sql
│   │       │   ├── V2__catalog.sql
│   │       │   └── V3__product.sql
│   │       └── keys/
│   │           ├── private.pem          ← Sinh bằng openssl
│   │           └── public.pem           ← Sinh bằng openssl
│   └── test/
├── .env.properties                      ← Tự tạo, KHÔNG commit
├── .env.properties.example              ← Commit để team biết schema
├── .gitignore
├── mvnw / mvnw.cmd                      ← Maven wrapper
├── pom.xml
└── README.md
```

=---

## 🐛 Troubleshooting### ❌ `Connection refused: localhost:5432`

PostgreSQL chưa chạy.

```bash
# Linux
sudo systemctl start postgresql

# macOS
brew services start postgresql@17

# Windows
# Mở Services > PostgreSQL > Start
```

### ❌ `Connection refused: localhost:6379`

Redis chưa chạy.

```bash
# Linux
sudo systemctl start redis-server

# macOS
brew services start redis

# Windows (Docker)
docker start petcare-redis
```

### ❌ `Database "veterinaryshop" does not exist`

Bạn chưa tạo database. Quay lại bước [Tạo database PostgreSQL](#2-tạo-database-postgresql).

### ❌ `FATAL: password authentication failed for user "postgres"`

Mật khẩu trong `.env.properties` không khớp với password của user `postgres` trong PostgreSQL.

- Đổi password trong file `.env.properties`
- Hoặc đổi password Postgres: `sudo -u postgres psql` rồi `\password postgres`

### ❌ `Could not load private key from classpath:keys/private.pem`

Bạn chưa sinh RSA keys. Quay lại bước [Sinh RSA key pair](#4-sinh-rsa-key-pair-public--private).

### ❌ `Flyway migration checksum mismatch`

Bạn đã sửa file `V*__*.sql` sau khi đã apply.

```bash
# Cách 1: Tạo migration mới V[next]__fix.sql
# Cách 2 (DEV ONLY - sẽ mất data): Drop database
psql -U postgres
DROP DATABASE veterinaryshop;
CREATE DATABASE veterinaryshop;
# Rồi run app lại
```

### ❌ Port 8990 đã bị chiếm

```bash
# Linux/macOS
lsof -i :8990
kill -9 <PID>

# Windows
netstat -ano | findstr :8990
taskkill /PID <PID> /F
```

### ❌ CORS error khi gọi từ FE

File `.env.properties`, thêm origin FE vào `CORS_ALLOWED_ORIGINS`:

```properties
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5275
```

Sau đó restart BE.

### ❌ Lombok không generate getter/setter

- IntelliJ: cài plugin **Lombok** → Settings → Build → Compiler → Annotation Processors → ✅ Enable annotation processing
- VS Code: cài extension **Lombok Annotations Support for VS Code**

### ❌ MapStruct: class not found

```bash
mvn clean compile
# Hoặc trong IntelliJ: Build > Rebuild Project
```

=---

## 📚 Tài liệu liên quan

- [Spring Boot 3.5 Reference](https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/)
- [Flyway Documentation](https://flywaydb.org/documentation/)
- [JWT RFC 7519](https://datatracker.ietf.org/doc/html/RFC7519)
- [OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Frontend repo](#) (link sau khi push FE)

---

## 📄 License

MIT License — Đồ án tốt nghiệp 2026## 👨‍💻 Tác giả

[Your Name] — [your.email@gmail.com]
