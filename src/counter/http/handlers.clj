(ns counter.http.handlers
  (:require
   [cheshire.core :as json]
   [clojure.string :as string]
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

(defn- parse-long
  [value]
  (cond
    (number? value) value
    (string? value) (try
                      (Long/parseLong value)
                      (catch NumberFormatException _
                        (throw (ex-info "invalid id" {:type :invalid-id}))))
    :else nil))

(defn health-handler
  [_request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "ok"})

(defn get-count
  [request]
  (let [conn (:db/conn request)
        counter-id (parse-long (get-in request [:query-params :id]))]
    (when (nil? counter-id)
      (throw (ex-info "id is required" {:type :missing-id})))
    (json-response 200 {:count (service/get-count conn counter-id)})))

(defn increment-count
  [request]
  (let [conn (:db/conn request)
        counter-id (parse-long (get-in request [:json-params :counter-id]))
        increment-value (get-in request [:json-params :increment-value])]
    (when (nil? counter-id)
      (throw (ex-info "id is required" {:type :missing-id})))
    (when-not (number? increment-value)
      (throw (ex-info "increment value must be a number" {:type :invalid-increment-type})))
    (json-response 200 {:count (service/increment! conn counter-id increment-value)})))

(defn reset-count
  [request]
  (let [conn (:db/conn request)]
    (let [counter-id (parse-long (get-in request [:json-params :counter-id]))]
      (json-response 200 {:count (service/reset! conn counter-id)}))))

(defn get-counters
  [request]
  (let [conn (:db/conn request)]
    (json-response 200 {:counters (service/get-counters conn)})))

(defn create-counter
  [request]
  (let [conn (:db/conn request)]
    (println "[handler] create-counter called")
    (let [name (get-in request [:json-params :name])]
      (when (string/blank? name)
        (throw (ex-info "name is required" {:type :invalid-name})))
      (json-response 201 (service/create-counter conn name)))))

(defn delete-counter
  [request]
  (let [conn (:db/conn request)]
    (let [id (parse-long (get-in request [:query-params :id]))]
      (when (nil? id)
        (throw (ex-info "id is required" {:type :missing-id})))
      (service/delete-counter conn id)
      (empty-response 204))))
