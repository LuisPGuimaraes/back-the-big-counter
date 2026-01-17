(ns counter.infra.db.datomic.schema
  (:require [datomic.client.api :as d]))

(def counter-schema
  [{:db/ident       :counter/value
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "A counter value"}

   {:db/ident       :counter/updated-at
    :db/valueType   :db.type/instant
    :db/cardinality :db.cardinality/one
    :db/doc         "Timestamp of the last update"}

   {:db/ident       :counter/action
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "The action performed on the counter"}

   {:db/ident       :counter/user-id
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Identifier for the user associated with the counter"}])

(def user-schema
  [{:db/ident       :user/username
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "The username of the user"}

   {:db/ident       :user/email
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "The email of the user"} 
   
   {:db/ident       :user/password-hash
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Hashed password of the user"}
   
   {:db/ident       :user/token
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Authentication token for the user"}])

(defn apply-schema!
  [conn]
  (d/transact conn {:tx-data (concat counter-schema user-schema)}))
