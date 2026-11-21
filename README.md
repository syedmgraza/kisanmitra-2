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

Plant disease checks use PlantHealthEngine's `POST /api/v1/diagnose` endpoint. Set `PLANT_HEALTH_API_KEY` in your shell or hosting provider's secret settings; never commit a real key. The request sends a multipart `image` and `crop` (`tomato`, `potato`, or `maize`) with a Bearer token. The application defaults `PLANT_HEALTH_API_URL` to `https://planthealthengine.com/api/v1/diagnose`. Spring Boot does not load `.env` automatically. Requests time out after ten seconds. API output includes a result status, disease, confidence, and treatment guidance. The free provider plan is limited by its current quota and rate limits.

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

`render.yaml` defines free Render web services for Spring Boot and FastAPI, plus a free Render Postgres database. The ML service is public so the Spring service can call it on the free plan; its URL is configured in `ML_SERVICE_URL`. Blueprint setup prompts for `PLANT_HEALTH_API_KEY`; provide it as a secret and never commit it. The free Render Postgres database expires after 30 days, and free web services sleep when idle. These settings are for demos, not durable production hosting.

## Model and data provenance

The two pickle models and supporting CSVs were taken from the repository named in the project brief and are packaged under `ml-service/`. The crop recommendation model follows its historical district-yield workflow; the yield model returns a transformed quintals-per-hectare estimate. Weather Explorer displays the supplied historical state/season averages, not live forecasts. Model and market estimates are planning aids and should be checked against local conditions.

Signup state and district options are generated from the crop recommendation history in `ml-service/final_data.csv`. This prevents unsupported free-text locations, though a supported district may still lack records for a particular season.

Python dependency versions are pinned in `ml-service/requirements.txt` for reproducible installs. The source repository does not record the sklearn training version explicitly; if model compatibility warnings appear, reproduce the exact training environment before upgrading or re-pickling either model.
