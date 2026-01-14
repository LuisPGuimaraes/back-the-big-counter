(ns counter.db.datomic
  (:require [com.datomic/client.api :as d]))

(def default-config
  {:server-type :dev-local
   :system "dev"
   :storage-dir "data/datomic"})

(def default-db-name "counter")

(defn- env-or
  [k fallback]
  (or (System/getenv k) fallback))

(defn client-config []
  {:server-type (:server-type default-config)
   :system (env-or "DATOMIC_SYSTEM" (:system default-config))
   :storage-dir (env-or "DATOMIC_STORAGE_DIR" (:storage-dir default-config))})

(defn client []
  (d/client (client-config)))

(defn db-name []
  (env-or "DATOMIC_DB_NAME" default-db-name))

(defn connect []
  (let [client (client)
        db-name (db-name)]
    (d/create-database client {:db-name db-name})
    (d/connect client {:db-name db-name})))

(defn release
  [conn]
  (when conn
    (d/release conn)))
