# BFHL REST API — Chitkara Java Round (27 June 2026)

## Quick Start (local)

```bash
# 1. Clone / unzip the project
cd bfhl-api

# 2. Set your details in src/main/resources/application.properties
#    app.user.full-name=your_name
#    app.user.dob=ddmmyyyy
#    app.user.email=your@email.com
#    app.user.roll-number=YOURROLL

# 3. Run
mvn spring-boot:run

# 4. Test
curl -X POST http://localhost:8080/bfhl \
     -H "Content-Type: application/json" \
     -d '{"data":["a","1","334","4","R","$"]}'
```

## Run Tests

```bash
mvn test
```

---

## Deploy to Railway (recommended)

1. Push this folder to a GitHub repo.
2. Go to [railway.app](https://railway.app) → New Project → Deploy from GitHub.
3. Railway auto-detects the Dockerfile and builds it.
4. Once deployed, copy the public URL (e.g. `https://bfhl-xxx.up.railway.app`).
5. Submit `https://bfhl-xxx.up.railway.app/bfhl` in the form.

## Deploy to Render

1. Push to GitHub.
2. New Web Service → connect repo.
3. Runtime: Docker (auto-detected) or set Build Command `mvn package -DskipTests` and Start Command `java -jar target/bfhl-1.0.0.jar`.
4. Copy the Render URL and submit `/bfhl`.

---

## API Reference

### POST /bfhl
**Request**
```json
{ "data": ["a", "1", "334", "4", "R", "$"] }
```

**Response (200)**
```json
{
  "is_success": true,
  "user_id": "john_doe_17091999",
  "email": "john@xyz.com",
  "roll_number": "ABCD123",
  "odd_numbers": ["1"],
  "even_numbers": ["334", "4"],
  "alphabets": ["A", "R"],
  "special_characters": ["$"],
  "sum": "339",
  "concat_string": "Ra"
}
```

### GET /health
Returns `{"status":"UP"}` — used by hosting platforms for liveness checks.
