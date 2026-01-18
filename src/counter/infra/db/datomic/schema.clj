(ns counter.infra.db.datomic.schema
  (:require [datomic.client.api :as d]))

(def counter-schema
  [{:db/ident       :counter/value
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "A counter value"}
   
   {:db/ident       :counter/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Name of the counter"} 

   {:db/ident       :counter/updated-at
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Timestamp of the last update"}

   {:db/ident       :counter/action
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "The action performed on the counter"}

   {:db/ident       :counter/enabled
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc         "Whether the counter is enabled"}])


(defn apply-schema!
  [conn]
  (d/transact conn {:tx-data (concat counter-schema)}))
