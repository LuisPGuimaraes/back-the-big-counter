(ns counter.http.interceptors
  (:require [io.pedestal.interceptor :as interceptor]))

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
