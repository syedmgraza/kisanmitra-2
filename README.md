# KisanMitra

KisanMitra is a farmer-facing crop planning and local marketplace app. Spring Boot 3.4 / Java 17 serves the Thymeleaf UI and authenticated workflows. A private FastAPI service loads the supplied crop and yield models once at process startup. Disease checks are forwarded only to an external API configured by the operator.

## Project layout

```text
kisanmitra/
├── spring-backend/       Spring Boot app, templates and static assets
├── ml-service/           FastAPI inference service, models and reference data
├── .env.example
└── render.yaml
```

## Local run

Open two terminals from this project directory.

Terminal 1:

```bash
cd ml-service
python -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn api:app --host 0.0.0.0 --port 8000
```

Terminal 2:

```bash
cd spring-backend
./mvnw spring-boot:run
```

Visit `http://localhost:8080`. H2 is in-memory for local development; data is cleared when that process stops. Register a farmer account to use the modules.

## Plant health API

Disease detection requires an external service that accepts a multipart field named `image` and an `x-api-key` header. Set `PLANT_HEALTH_API_URL` to its endpoint and `PLANT_HEALTH_API_KEY` to its key before starting Spring Boot. The committed `.env.example` uses the dummy value `abcd`; that value intentionally disables requests and displays “API key not configured.” Spring Boot does not load `.env` automatically, so export the variables in your shell or use your deployment platform's environment settings. Requests time out after ten seconds. The external service's response should include a disease/label, confidence (0–100 or 0–1), and optional treatment/advice fields.

All plant health output is guidance only, not a medical diagnosis.

## PostgreSQL

H2 is the default. To use PostgreSQL, include the PostgreSQL JDBC driver already declared in the Maven file and set:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://HOST:5432/DBNAME?sslmode=require'
export SPRING_DATASOURCE_USERNAME='DBUSER'
export SPRING_DATASOURCE_PASSWORD='DBPASSWORD'
export SPRING_DATASOURCE_DRIVER='org.postgresql.Driver'
export DDL_AUTO='validate'
```

Use a managed PostgreSQL service and a schema migration tool before production use; Hibernate `update` is the local default, while production should use reviewed migrations and `validate`.

## Render deployment

`render.yaml` defines the public Spring Boot web service and private ML service. Configure the database connection variables in the Spring service. Add `PLANT_HEALTH_API_URL` and set `PLANT_HEALTH_API_KEY` as a Render secret in the service's Environment settings; do not commit a live key. The external disease API must support the request format described above. The ML service is private and is reached over Render's internal service address.

## Model and data provenance

The two pickle models and supporting CSVs were taken from the repository named in the project brief and are packaged under `ml-service/`. The crop recommendation model follows its historical district-yield workflow; the yield model returns a transformed quintals-per-hectare estimate. Weather Explorer displays the supplied historical state/season averages, not live forecasts. Model and market estimates are planning aids and should be checked against local conditions.

Python dependency versions are pinned in `ml-service/requirements.txt` for reproducible installs. The source repository does not record the sklearn training version explicitly; if model compatibility warnings appear, reproduce the exact training environment before upgrading or re-pickling either model.
