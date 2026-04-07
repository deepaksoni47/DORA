# DORA Architecture

## System Style

DORA should begin as a modular monolith with clean internal boundaries.
This is the best fit for the current stack and project scope.

## Finalized Stack

### Frontend

- Next.js
- Tailwind CSS

### Backend

- Spring Boot
- Spring Data JPA
- REST API

### Database

- PostgreSQL

### Crawling

- Jsoup

### External Integrations

- YouTube API
- GitHub API
- arXiv API
- Wikipedia API

### Tools

- Maven
- Git
- Postman

### Frontend

- Framework: Next.js
- Styling: Tailwind CSS
- Responsibility:
  - search input and filters
  - categorized result display
  - pagination and sorting UI
  - future saved searches and bookmarks

### Backend

- Framework: Spring Boot
- Persistence: Spring Data JPA
- API Style: REST
- Responsibility:
  - expose search APIs
  - manage source connectors
  - run crawling and ingestion jobs
  - store normalized educational resources
  - perform indexing and ranking

## High-Level Modules

### 1. Search API

Handles incoming search requests from the frontend.

Example responsibilities:

- parse query text
- validate filters
- call ranking and retrieval services
- return grouped results

### 2. Source Connectors

Integrates with external educational sources through APIs.

Planned examples:

- arXiv for research papers
- YouTube for educational videos
- GitHub for code repositories
- Wikipedia for concept summaries and background material

### 3. Crawler

Collects data from selected educational websites with controlled scope.

Implementation tool:

- Jsoup for HTML fetching and parsing

Rules:

- only crawl open and permitted sites
- respect robots policies and rate limits
- focus on educational pages, not general web crawling

### 4. Content Store

Stores normalized search documents and metadata.

Core fields:

- title
- description or abstract
- url
- source name
- content type
- authors or creator
- publication year
- tags or keywords
- fetched timestamp

### 5. Indexing and Ranking

Supports fast retrieval and useful ordering.

Early ranking signals:

- keyword match
- title match boost
- source priority
- freshness or recency
- content type relevance

## Suggested Backend Package Layout

```text
backend/src/main/java/com/dora/search/
  config/
  search/
    api/
    application/
    domain/
    infrastructure/
  source/
    api/
    application/
    domain/
    infrastructure/
  crawler/
  indexing/
  common/
```

Meaning:

- `api/` contains controllers and request or response DTOs
- `application/` contains use cases and service orchestration
- `domain/` contains models and business rules
- `infrastructure/` contains persistence and external API adapters

## Suggested Frontend Layout

```text
frontend/src/
  app/
  components/
  features/
    search/
  lib/
  styles/
```

Meaning:

- `app/` contains routes and layouts
- `components/` contains reusable UI pieces
- `features/search/` contains search-specific UI and state
- `lib/` contains API clients and shared helpers
- `styles/` contains global theme and design tokens

## Initial API Direction

Start with a single search endpoint:

`GET /api/search?q=machine+learning&type=paper&years=2026`

Later we can add:

- `GET /api/v1/sources`
- `POST /api/v1/crawl/jobs`
- `POST /api/v1/index/rebuild`
- `GET /api/v1/health`

## Data Strategy

Use a relational database first for simplicity and traceability.

- PostgreSQL for normalized metadata
- Spring Data JPA for entity mapping and repository access
- optional full-text support at first
- later move to Elasticsearch or OpenSearch if advanced ranking is needed

## External Source Plan

### YouTube API

Use for educational video discovery, titles, channels, thumbnails, and publish dates.

### GitHub API

Use for repositories, README metadata, stars, language, and educational project links.

### arXiv API

Use for research paper titles, authors, abstracts, and publication metadata.

### Wikipedia API

Use for topic summaries and concept-oriented reference material.

## Report Strength

This stack is strong for an academic project report because it shows:

- a modern frontend using Next.js and Tailwind CSS
- a structured enterprise backend using Spring Boot and JPA
- a real relational database with PostgreSQL
- both API integration and crawler-based data collection
- practical software engineering tooling with Maven, Git, and Postman

## Recommended Build Order

1. Build the backend search API contract.
2. Build the frontend search page and filter shell.
3. Add one or two API-based connectors.
4. Store normalized results in the database.
5. Add indexing and ranking improvements.
6. Add controlled crawling for selected sources.

## Why Not Microservices Yet

Microservices would add deployment and debugging complexity too early.
This project benefits more from strong package boundaries than separate services.
If the project grows, connectors, crawler, and indexer can later be extracted.
