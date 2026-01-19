(ns counter.http.interceptors
  (:require [cheshire.core :as json]
            [io.pedestal.interceptor :as interceptor]))

(defn- json-response
  [status data]
  {:status status
   :headers {"content-type" "application/json"}
   :body (json/generate-string data)})



(defn- error-response
  [status message]
  (json-response status {:error message}))

(defn error-handler
  []
  (interceptor/interceptor
   {:name ::error-handler
    :error (fn [context ex]
             (println "[handler] error:" (.getMessage ex))
             (if (instance? clojure.lang.ExceptionInfo ex)
               (let [{:keys [type]} (ex-data ex)
                     response (case type
                                :invalid-increment-type (error-response 400 "increment value must be a number")
                                :invalid-increment (error-response 400 "increment value must be greater than 0")
                                :invalid-name (error-response 400 "name is required")
                                :counter-already-exists (error-response 409 "counter with this name already exists")
                                :missing-id (error-response 400 "id is required")
                                :invalid-id (error-response 400 "id is invalid")
                                :counter-not-found (error-response 404 "counter not found")
                                (error-response 400 "bad request"))]
                 (assoc context :response response))
               (assoc context :response (error-response 500 "internal server error"))))}))

(defn- cors-headers
  []
  {"Access-Control-Allow-Origin" "*"
   "Access-Control-Allow-Methods" "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Max-Age" "86400"})

(defn cors
  "Pedestal interceptor that adds permissive CORS headers and handles preflight."
  []
  (interceptor/interceptor
   {:name ::cors
    :enter (fn [{:keys [request] :as context}]
             (if (= :options (:request-method request))
               (assoc context :response {:status 200
                                         :headers (cors-headers)
                                         :body ""})
               context))
    :leave (fn [context]
             (update-in context [:response :headers]
                        (fnil merge {})
                        (cors-headers)))}))

(defn inject-db
  "Pedestal interceptor that attaches the Datomic connection to the request."
  [system]
  (interceptor/interceptor
   {:name ::inject-db
    :enter (fn [context]
             (assoc-in context [:request :db/conn] (:db/conn system)))}))
