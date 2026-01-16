(ns counter.http.handlers)

(defn health-handler
  [_request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "ok"})

(defn get-count
  [_request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"count\": 42}"})

(defn increment-count
  [_request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"count\": 43}"})

(defn reset-count
  [_request]
  {:status 200
   :headers {"content-type" "application/json"}
   :body "{\"count\": 0}"})