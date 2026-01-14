(ns counter.system
  (:require [counter.http.routes :as routes]
            [io.pedestal.http :as http]))

(def service
  {::http/routes routes/routes
   ::http/type :jetty
   ::http/port 3000
   ::http/join? true})

(defn start []
  (println "Starting server on port 3000")
  (http/start (http/create-server service)))
