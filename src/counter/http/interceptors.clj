(ns counter.http.interceptors
  (:require [cheshire.core :as json]
            [counter.errors :as errors]
            [counter.logging :as logging]
            [io.pedestal.interceptor :as interceptor]))

(defn- cors-headers
  []
  {"Access-Control-Allow-Origin" "*"
   "Access-Control-Allow-Methods" "GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD"
   "Access-Control-Allow-Headers" "*"
   "Access-Control-Max-Age" "86400"})

(defn- json-response
  [status data]
  {:status status
   :headers {"content-type" "application/json"}
   :body (json/generate-string data)})

(defn- error-response
  [status type message]
  (update (json-response status {:error {:type type
                                         :message message}})
          :headers
          merge
          (cors-headers)))

(defn error-interceptor
  []
  (interceptor/interceptor
   {:name ::error-handler
    :error (fn [context ex]
             (let [type (when (instance? clojure.lang.ExceptionInfo ex)
                          (:type (ex-data ex)))
                   ex-message (.getMessage ex)]
               (logging/log-error {:type type :message ex-message} "[handler] error")
             (if (instance? clojure.lang.ExceptionInfo ex)
                 (let [{:keys [status message]} (errors/error-definition type)
                       response (error-response status
                                                type
                                                (or ex-message message))]
                   (assoc context :response response))
                 (assoc context :response (error-response 500
                                                          :internal-server-error
                                                          "internal server error")))))}))

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
