# SocialHub

[![CI](https://github.com/enesmemduhoglu/socialHub/actions/workflows/ci.yml/badge.svg)](https://github.com/enesmemduhoglu/socialHub/actions/workflows/ci.yml)

Spring Boot ile geliştirilmiş bir sosyal platform backend'i. Kullanıcılar kayıt olup gönderi paylaşır, birbirini takip eder; kişiselleştirilmiş bir akış (feed), beğeni/yorum ve bildirim sistemi bulunur. Proje; ölçeklenebilirlik (keyset sayfalama, N+1 önleme, Redis cache), güvenlik (JWT, rate limiting) ve test/CI-CD pratiklerini sergilemek amacıyla tasarlanmıştır.

## Özellikler

- **Kimlik doğrulama** — JWT tabanlı stateless auth; kullanıcı adı **veya** e-posta ile giriş, BCrypt parola, varsayılan-secret açılış uyarısı
- **Profil** — `PATCH /api/users/me` ile kısmi güncelleme (null alan değişmez, boş string temizler)
- **Gönderi CRUD** — sahiplik kontrolü, DTO validasyonu, **cursor (keyset) sayfalama** (offset yok; `(author_id, id DESC)` bileşik indeksi)
- **Takip** — self-referencing M-N, unique + self-follow CHECK kısıtları
- **Feed** — kendi + takip edilenlerin gönderileri tek sorguda (`IN` + join fetch → **N+1 yok**); takip edilen id listesi **Redis'te cache'lenir** (TTL 10 dk, follow/unfollow'da evict)
- **Beğeni & Yorum** — beğeni **idempotent** (unique kısıt + yarış durumu toleransı); yorumlar cursor sayfalamalı
- **Bildirim** — follow/like/comment olayları Spring domain event'leriyle üretilir (`@TransactionalEventListener(AFTER_COMMIT)` + `REQUIRES_NEW`); okundu işaretleme ve okunmamış sayacı
- **Sertleştirme** — Bucket4j ile `/api/auth/**` üzerinde IP başına rate limit (429), env ile yönetilen CORS, tutarlı `ApiError` gövdesiyle global hata yönetimi (400/401/403/404/409/429/500)
- **API dokümantasyonu** — springdoc-openapi ile otomatik OpenAPI 3.1 şeması + Swagger UI

## Teknolojiler

Java 21 · Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Actuator) · PostgreSQL 16 + Flyway · Redis 7 (Spring Cache) · JWT (jjwt) · Bucket4j · springdoc-openapi · Docker (multi-stage imaj) + Docker Compose · GitHub Actions · JUnit 5 + MockMvc

## Mimari

Katman-bazlı değil, **feature-bazlı** paketleme: her özellik kendi `model / dto / repository / service / controller` paketlerini içerir.

```
com.enes.social
├── auth           # kayıt, giriş, JWT üretimi
├── user           # profil görüntüleme/düzenleme
├── post           # gönderi CRUD + sayfalama
├── follow         # takip ilişkileri, profil istatistikleri
├── feed           # kişisel akış (timeline)
├── like           # idempotent beğeni
├── comment        # yorumlar
├── notification   # event tabanlı bildirimler
├── security       # JWT filtresi, SecurityConfig, rate limit
└── common         # ApiError, CursorPageResponse, cache/openapi config
```

Veritabanı şeması Flyway migration'larıyla yönetilir (`src/main/resources/db/migration`, V1–V7).

## Kurulum ve Çalıştırma

**Önkoşullar:** JDK 21, Docker Desktop. (Maven kurulumu gerekmez — `mvnw` wrapper kullanılır.)

1. Ortam dosyasını oluşturun:

   ```bash
   cp .env.example .env
   ```

   Geliştirme için varsayılan değerler yeterli; yalnızca `JWT_SECRET` için uzun ve rastgele bir değer vermeniz önerilir. **Not:** PostgreSQL host'ta `5433` portuna eşlenmiştir (5432 çakışmalarını önlemek için).

2. Çalıştırma — iki seçenek:

   **a) Geliştirme modu** (uygulama host'ta, bağımlılıklar container'da):

   ```bash
   docker compose up -d postgres redis
   ./mvnw spring-boot:run
   ```

   **b) Tamamı container'da** (multi-stage Docker imajı):

   ```bash
   docker compose up -d --build app
   ```

3. Sağlık kontrolü: <http://localhost:8080/actuator/health> → `{"status":"UP"}`. Adminer (DB arayüzü): <http://localhost:8081>.

## API Dokümantasyonu (Swagger UI)

Uygulama ayaktayken:

- **Swagger UI:** <http://localhost:8080/swagger-ui.html>
- **OpenAPI şeması (JSON):** <http://localhost:8080/v3/api-docs>

Korumalı uçları denemek için: `POST /api/auth/register` veya `POST /api/auth/login` çağrısından dönen `accessToken` değerini kopyalayın → sağ üstteki **Authorize** butonuna yapıştırın → tüm uçlar "Try it out" ile kullanılabilir.

### Başlıca uçlar

| Alan | Uçlar |
|---|---|
| Auth | `POST /api/auth/register` · `POST /api/auth/login` |
| Profil | `GET /api/users/me` · `PATCH /api/users/me` · `GET /api/users/{username}` |
| Gönderi | `POST/GET /api/posts` · `GET/PUT/DELETE /api/posts/{id}` · `GET /api/users/{username}/posts` |
| Takip | `POST/DELETE /api/users/{username}/follow` · `GET .../followers` · `GET .../following` |
| Feed | `GET /api/feed` |
| Beğeni | `POST/DELETE /api/posts/{id}/like` |
| Yorum | `POST/GET /api/posts/{id}/comments` · `DELETE /api/posts/{id}/comments/{commentId}` |
| Bildirim | `GET /api/notifications` · `GET .../unread-count` · `POST .../{id}/read` · `POST .../read-all` |

Listeleme uçları **cursor sayfalama** kullanır: `?cursor=<sonKayıtId>&size=<adet>` (maks. 50); yanıttaki `nextCursor` bir sonraki sayfanın girdisidir, `hasMore=false` ise liste bitmiştir.

## Testler

Testler gerçek bir PostgreSQL'e karşı koşar (compose'daki sunucuda ayrı `socialhub_test` veritabanı, Spring `test` profili). İlk çalıştırmadan önce test veritabanını bir kez oluşturun:

```bash
docker compose up -d postgres
docker exec socialhub-postgres psql -U socialhub -d socialhub -c "CREATE DATABASE socialhub_test;"
```

Ardından:

```bash
./mvnw verify
```

Test altyapısı: MockMvc ile uçtan uca entegrasyon testleri (tam Spring bağlamı + güvenlik filtreleri) ve saf unit testler (ör. `JwtServiceTest`, Spring bağlamı olmadan). Test profili cache'i kapatır (`cache.type=none`) ve rate limit'i devre dışı bırakır; rate limit davranışı kendi testinde ayrıca doğrulanır.

## CI

Her push ve PR'da GitHub Actions ([ci.yml](.github/workflows/ci.yml)):

1. JDK 21 + Maven cache ile `./mvnw -B verify` — testler, lokaldekiyle birebir aynı yapılandırmada bir PostgreSQL service container'ına karşı koşar.
2. Multi-stage Docker imajının derlenebildiğini doğrular (buildx + GHA katman cache'i, registry'ye push yok).
