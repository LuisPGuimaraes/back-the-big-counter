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

## Endpoints and contracts

Base URL: `http://localhost:3000`

### GET /health

Response:

- 200 text/plain
- Body: `ok`

### GET /count

Query params:

- `id` (required, number)

Response:

- 200 application/json
- Body: `{"count": <number>}`

### POST /count/increment

JSON body:

- `counter-id` (required, number)
- `increment-value` (required, number > 0)

Response:

- 200 application/json
- Body: `{"count": <number>}`

### POST /count/reset

JSON body:

- `counter-id` (required, number)

Response:

- 200 application/json
- Body: `{"count": <number>}`

### GET /counter

Response:

- 200 application/json
- Body: `{"counters": [{"id": <number>, "name": <string>, "value": <number>}]}`

### POST /counter/create

JSON body:

- `name` (required, string)

Response:

- 201 application/json
- Body: `{"id": <number>, "name": <string>}`

### DELETE /counter

Query params:

- `id` (required, number)

Response:

- 204 application/json
- Body: empty
