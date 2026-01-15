(ns counter.db.datomic
  (:require [com.datomic/client.api :as d]))

(defn client-config []
  {:server-type :dev-local
   :system "dev"
   :storage-dir "data/datomic"})

(defn client []
  (d/client (client-config)))

(defn db-name []
  "counter")

(defn conn []
  (let [client (client) db-name (db-name)]
    (d/create-database client {:db-name db-name})
    (d/connect client {:db-name db-name})))
