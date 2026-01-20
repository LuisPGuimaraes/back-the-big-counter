(ns counter.http.handlers
  (:require
   [cheshire.core :as json]
   [counter.application.counter-service :as service]
   [counter.errors :as errors]
   [counter.http.schemas :as schemas]
   [counter.logging :as logging]))

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
                        (throw (errors/error-info :invalid-id))))
    :else nil))

(defn health-handler
  [request]
  (logging/log-request :health-handler request)
  (let [response {:status 200
                  :headers {"content-type" "text/plain"}
                  :body "ok"}]
    (logging/log-response :health-handler response)
    response))

(defn get-count
  [request]
  (logging/log-request :get-count request)
  (let [conn (:db/conn request)
        counter-id (parse-id (get-in request [:query-params :id]))]
    (when (nil? counter-id)
      (throw (errors/error-info :missing-id)))
    (schemas/validate-request! schemas/GetCountQuery {:id counter-id} :invalid-id)
    (let [body {:count (service/get-count conn counter-id)}]
      (schemas/validate-response! schemas/CountResponse body)
      (let [response (json-response 200 body)]
        (logging/log-response :get-count response)
        response))))

(defn increment-count
  [request]
  (logging/log-request :increment-count request)
  (let [conn (:db/conn request)
        counter-id (parse-id (get-in request [:json-params :counter-id]))
        increment-value (get-in request [:json-params :increment-value])]
    (when (nil? counter-id)
      (throw (errors/error-info :missing-id)))
    (schemas/validate-request! schemas/IncrementBody
                               {:counter-id counter-id
                                :increment-value increment-value}
                                :invalid-increment-type)
    (let [body {:count (service/increment! conn counter-id increment-value)}]
      (schemas/validate-response! schemas/CountResponse body)
      (let [response (json-response 200 body)]
        (logging/log-response :increment-count response)
        response))))

(defn reset-count
  [request]
  (logging/log-request :reset-count request)
  (let [conn (:db/conn request)
        counter-id (parse-id (get-in request [:json-params :counter-id]))]
    (when (nil? counter-id)
      (throw (errors/error-info :missing-id)))
    (schemas/validate-request! schemas/ResetBody {:counter-id counter-id} :invalid-id)
    (let [body {:count (service/reset-counter! conn counter-id)}]
      (schemas/validate-response! schemas/CountResponse body)
      (let [response (json-response 200 body)]
        (logging/log-response :reset-count response)
        response))))

(defn get-counters
  [request]
  (logging/log-request :get-counters request)
  (let [conn (:db/conn request)
        body {:counters (service/get-counters conn)}]
    (schemas/validate-response! schemas/CountersResponse body)
    (let [response (json-response 200 body)]
      (logging/log-response :get-counters response)
      response)))

(defn create-counter
  [request]
  (logging/log-request :create-counter request)
  (let [conn (:db/conn request)
        name (get-in request [:json-params :name])]
    (schemas/validate-request! schemas/CreateCounterBody {:name name} :invalid-name)
    (let [body (service/create-counter conn name)]
      (schemas/validate-response! schemas/CounterResponse body)
      (let [response (json-response 201 body)]
        (logging/log-response :create-counter response)
        response))))

(defn delete-counter
  [request]
  (logging/log-request :delete-counter request)
  (let [conn (:db/conn request)
        id (parse-id (get-in request [:query-params :id]))]
    (when (nil? id)
      (throw (errors/error-info :missing-id)))
    (schemas/validate-request! schemas/GetCountQuery {:id id} :invalid-id)
    (service/delete-counter conn id)
    (let [response (empty-response 204)]
      (logging/log-response :delete-counter response)
      response)))
