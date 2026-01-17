(ns counter.http.interceptors
  (:require [io.pedestal.interceptor :as interceptor]))

(defn inject-db
  "Pedestal interceptor that attaches the Datomic connection to the request."
  [system]
  (interceptor/interceptor
   {:name ::inject-db
    :enter (fn [context]
             (assoc-in context [:request :db/conn] (:db/conn system)))}))
