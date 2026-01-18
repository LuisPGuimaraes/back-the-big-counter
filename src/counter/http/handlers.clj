(ns counter.http.handlers
  (:require
   [clojure.string :as string]
   [counter.application.counter-service :as service]))

(defn- json-count
  [count-value]
  (format "{\"count\": %s}" count-value))

(defn- json-error
  [message]
  (format "{\"error\": \"%s\"}" message))

(defn- json-list-counters
  [counters]
  (let [counters-json (map (fn [{:keys [name id]}]
                             (format "{\"name\": \"%s\", \"id\": %d}" name id)) counters)]
    (format "{\"counters\": [%s]}" (string/join "," counters-json))))

(defn- json-counter
  [{:keys [name id]}]
  (format "{\"name\": \"%s\", \"id\": %d}" name id))

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

(defn get-counters
  [request]
  (let [conn (:db/conn request)]
    (try
      (response 200 (json-list-counters (service/get-counters conn)))
      (catch Exception ex
        (println "[handler] get-counters error:" (.getMessage ex))
        (response 400 (json-error "error while getting counters"))))))

(defn create-counter
  [request]
  (let [conn (:db/conn request)]
    (try
      (let [name (get-in request [:json-params :name])]
        (response 201 (json-counter (service/create-counter conn name))))
      (catch Exception ex
        (println "[handler] create-counter error:" (.getMessage ex))
        (if (= :counter-already-exists (:type (ex-data ex)))
          (response 401 (json-error "counter with this name already exists"))
          (response 400 (json-error "error while creating counter")))))))
