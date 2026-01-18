(ns counter.http.handlers
  (:require
   [cheshire.core :as json]
   [counter.application.counter-service :as service]))

(defn- json-response
  [status data]
  {:status status
   :headers {"content-type" "application/json"}
   :body (json/generate-string data)})

(defn- empty-response
  [status]
  {:status status
   :headers {"content-type" "application/json"}
   :body ""})

(defn health-handler
  [_request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "ok"})

(defn get-count
  [request]
  (let [conn (:db/conn request)]
    (try
      (json-response 200 {:count (service/get-count conn)})
      (catch Exception ex
        (println "[handler] get-count error:" (.getMessage ex))
        (json-response 400 {:error "error while getting count"})))))

(defn increment-count
  [request]
  (let [conn (:db/conn request)]
    (try
      (json-response 200 {:count (service/increment! conn)})
      (catch Exception ex
        (println "[handler] increment-count error:" (.getMessage ex))
        (json-response 400 {:error "error while incrementing count"})))))

(defn reset-count
  [request]
  (let [conn (:db/conn request)]
    (try
      (json-response 200 {:count (service/reset! conn)})
      (catch Exception ex
        (println "[handler] reset-count error:" (.getMessage ex))
        (json-response 400 {:error "error while resetting count"})))))

(defn get-counters
  [request]
  (let [conn (:db/conn request)]
    (try
      (json-response 200 {:counters (service/get-counters conn)})
      (catch Exception ex
        (println "[handler] get-counters error:" (.getMessage ex))
        (json-response 400 {:error "error while getting counters"})))))

(defn create-counter
  [request]
  (let [conn (:db/conn request)]
    (try
      (let [name (get-in request [:json-params :name])]
        (json-response 201 (service/create-counter conn name)))
      (catch Exception ex
        (println "[handler] create-counter error:" (.getMessage ex))
        (if (= :counter-already-exists (:type (ex-data ex)))
          (json-response 401 {:error "counter with this name already exists"})
          (json-response 400 {:error "error while creating counter"}))))))

(defn delete-counter
  [request]
  (let [conn (:db/conn request)]
    (try
      (let [id (some-> (get-in request [:path-params :id]) Long/parseLong)] 
        (service/delete-counter conn id)
        (empty-response 204))
      (catch Exception ex
        (println "[handler] delete-counter error:" (.getMessage ex))
        (case (:type (ex-data ex))
          :bad-request (json-response 400 {:error "id is required"})
          :counter-not-found (json-response 404 {:error "counter not found"})
          (json-response 400 {:error "error while deleting counter"}))))))
