(ns counter.http.handlers
  (:require [counter.application.counter-service :as service]))

(defn- json-count
  [count-value]
  (format "{\"count\": %s}" count-value))

(defn- json-error
  [message]
  (format "{\"error\": \"%s\"}" message))

(defn- response
  [status body]
  {:status status
   :headers {"content-type" "application/json"}
   :body body})

(defn health-handler
  [_request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "ok"})

(defn get-count
  [request]
  (let [conn (:db/conn request)]
    (try
      (response 200 (json-count (service/get-count conn)))
      (catch Exception ex
        (println "[handler] get-count error:" (.getMessage ex))
        (response 400 (json-error "error while getting count"))))))

(defn increment-count
  [request]
  (let [conn (:db/conn request)]
    (try
      (response 200 (json-count (service/increment! conn)))
      (catch Exception ex
        (println "[handler] increment-count error:" (.getMessage ex))
        (response 400 (json-error "error while incrementing count"))))))

(defn reset-count
  [request]
  (let [conn (:db/conn request)]
    (try
      (response 200 (json-count (service/reset! conn)))
      (catch Exception ex
        (println "[handler] reset-count error:" (.getMessage ex))
        (response 400 (json-error "error while resetting count"))))))
