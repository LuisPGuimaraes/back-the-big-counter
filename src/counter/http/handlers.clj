(ns counter.http.handlers
  (:require
   [cheshire.core :as json]
   [counter.application.counter-service :as service]
   [counter.http.schemas :as schemas]))

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

(defn- parse-id
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
        counter-id (parse-id (get-in request [:query-params :id]))]
    (when (nil? counter-id)
      (throw (ex-info "id is required" {:type :missing-id})))
    (schemas/validate-request! schemas/GetCountQuery {:id counter-id} :invalid-id)
    (let [body {:count (service/get-count conn counter-id)}]
      (schemas/validate-response! schemas/CountResponse body)
      (json-response 200 body))))

(defn increment-count
  [request]
  (let [conn (:db/conn request)
        counter-id (parse-id (get-in request [:json-params :counter-id]))
        increment-value (get-in request [:json-params :increment-value])]
    (when (nil? counter-id)
      (throw (ex-info "id is required" {:type :missing-id})))
    (schemas/validate-request! schemas/IncrementBody
                               {:counter-id counter-id
                                :increment-value increment-value}
                               :invalid-increment-type)
    (let [body {:count (service/increment! conn counter-id increment-value)}]
      (schemas/validate-response! schemas/CountResponse body)
      (json-response 200 body))))

(defn reset-count
  [request]
  (let [conn (:db/conn request)
        counter-id (parse-id (get-in request [:json-params :counter-id]))]
    (when (nil? counter-id)
      (throw (ex-info "id is required" {:type :missing-id})))
    (schemas/validate-request! schemas/ResetBody {:counter-id counter-id} :invalid-id)
    (let [body {:count (service/reset-counter! conn counter-id)}]
      (schemas/validate-response! schemas/CountResponse body)
      (json-response 200 body))))

(defn get-counters
  [request]
  (let [conn (:db/conn request)]
    (let [body {:counters (service/get-counters conn)}]
      (schemas/validate-response! schemas/CountersResponse body)
      (json-response 200 body))))

(defn create-counter
  [request]
  (let [conn (:db/conn request)]
    (println "[handler] create-counter called")
    (let [name (get-in request [:json-params :name])]
      (schemas/validate-request! schemas/CreateCounterBody {:name name} :invalid-name)
      (let [body (service/create-counter conn name)]
        (schemas/validate-response! schemas/CounterResponse body)
        (json-response 201 body)))))

(defn delete-counter
  [request]
  (let [conn (:db/conn request)
        id (parse-id (get-in request [:query-params :id]))]
    (when (nil? id)
      (throw (ex-info "id is required" {:type :missing-id})))
    (schemas/validate-request! schemas/GetCountQuery {:id id} :invalid-id)
    (service/delete-counter conn id)
    (empty-response 204)))
