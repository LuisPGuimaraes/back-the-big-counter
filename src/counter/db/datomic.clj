(ns counter.db.datomic
  (:require [datomic.client.api :as d]
            [counter.db.schema :as schema]))

(defn storage-dir []
  (-> (java.io.File. (System/getProperty "user.home") ".datomic/data")
      .getAbsolutePath))

(defn client-config []
  {:server-type :dev-local
   :system "dev"
   :storage-dir (storage-dir)})

(defn client []
  (d/client (client-config)))

(defn db-name []
  "counter")

(defn ensure-schema
  [conn]
  (let [missing (schema/missing-schema conn)]
    (when (seq missing)
      (d/transact conn {:tx-data missing}))))

(defn conn []
  (let [client (client) db-name (db-name)]
    (d/create-database client {:db-name db-name})
    (d/connect client {:db-name db-name})))
