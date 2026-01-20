(ns counter.logging
  (:require [clojure.tools.logging :as log]))

(defn- request-summary
  [request]
  {:method (:request-method request)
   :uri (:uri request)
   :query-params (:query-params request)
   :json-params (:json-params request)})

(defn log-request
  [handler-name request]
  (log/info handler-name "request" (request-summary request)))

(defn log-response
  [handler-name response]
  (log/info handler-name "response" (select-keys response [:status :body])))

(defn log-error
  [context message]
  (log/error message context))
