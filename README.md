# Kutubxona katalog xizmati

Kitoblar, mualliflar, kategoriyalar, a'zolar va kitob ijarasi uchun REST API
(Spring Boot 4, Java 17, PostgreSQL, Flyway).

## Talablar

- Java 17
- Docker (PostgreSQL va testlar uchun)

## Ishga tushirish

### 1. Ma'lumotlar bazasi

PostgreSQL `localhost:5432` da, `kutubxona` nomli baza bilan ishlab turishi kerak.
Agar hali yo'q bo'lsa:

```bash
docker run -d --name kutubxona-db -p 5432:5432 \
  -e POSTGRES_DB=kutubxona -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=<parol> \
  postgres:16
```

Konteyner to'xtagan bo'lsa: `docker start kutubxona-db`

### 2. Parolni sozlash (bir marta)

```bash
cp .env.example .env
# .env ichida DB_PASSWORD= ga parolni yozing
```

`.env` git'ga qo'shilmaydi. Ilova uni o'zi o'qiydi.

### 3. Ilovani ishga tushirish

**Terminal:**

```bash
./mvnw spring-boot:run
```

**IntelliJ IDEA:** yuqoridagi Run ro'yxatidan **Kutubxona (local)** ni tanlab ▶ bosing.

Birinchi ishga tushishda Flyway bazadagi sxemani yaratadi yoki yangilaydi.

### 4. Ochish

| Manzil | Nima |
|---|---|
| http://localhost:8080/swagger-ui/index.html | Barcha endpoint'lar, brauzerdan sinash mumkin |
| http://localhost:8080/api/books | Kitoblar ro'yxati |
| http://localhost:8080/api/loans | Ijaralar ro'yxati |

`http://localhost:8080/` da sahifa yo'q (404) — bu REST API, endpoint'lar `/api/...` ostida.

## Testlar

```bash
./mvnw test
```

Docker ishlab turishi kerak — PostgreSQL testlari Testcontainers orqali ishlaydi.
Har bir push va PR'da GitHub Actions'da ham avtomatik ishga tushadi.

## Sozlamalar

| O'zgaruvchi | Majburiy | Standart |
|---|---|---|
| `DB_PASSWORD` | ha | — |
| `DB_URL` | yo'q | `jdbc:postgresql://localhost:5432/kutubxona` |
| `DB_USERNAME` | yo'q | `postgres` |

Ijara qoidalari `src/main/resources/application.properties` da (`library.loan.*`):
bir a'zoga ko'pi bilan 5 ta kitob, muddat 14 kun, muddati o'tganlar har kuni 01:00 da belgilanadi.
