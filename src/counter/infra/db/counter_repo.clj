(ns counter.infra.db.counter-repo
  (:require [datomic.client.api :as d]))

(def counter-pull
  [:db/id
   :counter/name
   :counter/value
   :counter/enabled
   :counter/updated-at])

(defn now []
  (java.util.Date.))

(defn create-counter!
  [conn name]
  (let [tx @(d/transact conn
                        {:tx-data [{:counter/name name
                                    :counter/value 0
                                    :counter/enabled true
                                    :counter/action "create"
                                    :counter/updated-at (now)}]})
        id (-> tx :tempids vals first)]
    {:id id
     :name name}))

(defn find-counter-by-id
  [db id]
  (when (number? id)
    (d/pull db counter-pull id)))

(defn find-enabled-counter-by-name
  [db name]
  (ffirst
   (d/q '[:find ?e
          :in $ ?name
          :where
          [?e :counter/name ?name]
          [?e :counter/enabled true]]
        db
        name)))

(defn list-enabled-counters
  [db]
  (map (fn [[e]]
         (d/pull db counter-pull e))
       (d/q '[:find ?e
              :where
              [?e :counter/enabled true]]
            db)))

(defn save-count!
  [conn id value action]
  (d/transact conn
              {:tx-data [{:db/id id
                          :counter/value value
                          :counter/action action
                          :counter/updated-at (now)}]})
  value)

(defn increment-by!
  [conn id increment]
  (let [db (d/db conn)
        {:counter/keys [value]} (find-counter-by-id db id)
        next (+ value increment)]
    (save-count! conn id next "increment")))

(defn disable-counter!
  [conn id]
  (d/transact conn
              {:tx-data [{:db/id id
                          :counter/enabled false
                          :counter/action "disable"
                          :counter/updated-at (now)}]}))
