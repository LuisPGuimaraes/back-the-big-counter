(ns counter.system
  (:require [counter.http.interceptors :as interceptors]
            [counter.infra.db.datomic.datomic :as db]
            [counter.http.routes :as routes]
            [io.pedestal.http :as http]))

(def service
  {::http/routes routes/routes
   ::http/type :jetty
   ::http/port 3000
   ::http/join? true})

(defn create-system []
  (let [conn (db/conn)
        interceptor (interceptors/inject-db {:db/conn conn})
        service (-> service
                    http/default-interceptors
                    (update ::http/interceptors conj interceptor)
                    http/create-server)]
    {:db/conn conn
     :server service}))

(defn start []
  (let [system (create-system)]
    (println "Starting server on port 3000")
    (update system :server http/start)))
