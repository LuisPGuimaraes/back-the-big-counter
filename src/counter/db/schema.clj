(ns counter.db.schema
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

(def all-schema
  (concat counter-schema user-schema))

(defn schema-idents
  []
  (->> all-schema
       (map :db/ident)
       (remove nil?)
       set))

(defn missing-schema
  [conn]
  (let [idents (schema-idents)
        db (d/db conn)
        present (set
                  (map first
                       (d/q '[:find ?ident
                              :in $ [?ident ...]
                              :where [?e :db/ident ?ident]]
                            db
                            idents)))]
    (remove present all-schema)))
