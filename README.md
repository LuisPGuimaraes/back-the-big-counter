# back-end

Simple HTTP API for counters using Pedestal and Datomic (dev-local).

## Requirements

- Java (JDK 11+ recommended)
- Leiningen
- clj-kondo (for lint)

## Run

Start the server on port 3000:

```
lein run
```

Datomic dev-local data is stored in `~/.datomic/data` by default.

## Lint

```
clj-kondo --lint src test
```

## Tests

```
lein test
```

## Libraries

- Clojure 1.12.2
- Datomic Local 1.0.291
- Pedestal Service/Jetty 0.6.4
- Cheshire 5.13.0
- clj-http 3.12.3
- Prismatic Schema 1.4.1
- tools.logging 1.3.0 + slf4j-simple 2.0.13

## Endpoints and contracts

Base URL: `http://localhost:3000`

### GET /health

Response:

- 200 text/plain
- Body: `ok`

### GET /count

Query params:

- `id` (required, integer)

Response:

- 200 application/json
- Body: `{"count": <number>}`

### POST /count/increment

JSON body:

- `counter-id` (required, integer)
- `increment-value` (required, number > 0)

Response:

- 200 application/json
- Body: `{"count": <number>}`

### POST /count/reset

JSON body:

- `counter-id` (required, integer)

Response:

- 200 application/json
- Body: `{"count": <number>}`

### GET /counter

Response:

- 200 application/json
- Body: `{"counters": [{"id": <integer>, "name": <string>, "value": <integer>}]}` 

### POST /counter/create

JSON body:

- `name` (required, non-blank string)

Response:

- 201 application/json
- Body: `{"id": <integer>, "name": <string>}`

### DELETE /counter

Query params:

- `id` (required, integer)

Response:

- 204 application/json
- Body: empty
