(ns counter.system
  (:require [counter.db.datomic :as db]
            [counter.http.routes :as routes]
            [io.pedestal.http :as http]))

(def service
  {::http/routes routes/routes
   ::http/type :jetty
   ::http/port 3000
   ::http/join? true})

(defn create-system []
  {:db (db/connect)
   :server (http/create-server service)})

(defn start []
  (let [system (create-system)]
    (println "Starting server on port 3000")
    (update system :server http/start)))
