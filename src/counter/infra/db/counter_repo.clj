(ns counter.infra.db.counter-repo
  (:require [datomic.client.api :as d]))

(defn save-count!
  [conn value counter-id]
  (println "[repo] save-count! ->" value)
  (d/transact conn
              {:tx-data [{:counter/value value
                          :db/id counter-id
                          :counter/updated-at (java.util.Date.)}]})
  value)

(defn get-count-by-id
  [conn counter-id]
  (let [db (d/db conn)]
    (ffirst
     (d/q '[:find ?value
            :in $ ?id
            :where
            [?id :counter/value ?value]
            [?id :counter/enabled true]]
          db
          counter-id))))

(defn increment-by!
  [conn counter-id increment-value]
  (let [current (get-count-by-id conn counter-id)
        next (+ current increment-value)]
    (save-count! conn next counter-id)))

(defn create-counter!
  [conn name]
  (let [value 0]
    (println "[repo] create-counter! ->" name)
    (let [tx (d/transact conn
                         {:tx-data [{:counter/name name
                                     :counter/value value
                                     :counter/updated-at (java.util.Date.)
                                     :counter/action "create"
                                     :counter/enabled true}]})
          id (ffirst
              (d/q '[:find ?e
                     :in $ ?name
                     :where
                     [?e :counter/name ?name]
                     [?e :counter/enabled true]]
                   (:db-after tx)
                   name))]
      {:name name
       :id id})))

(defn list-counters
  [conn]
  (let [db (d/db conn)
        results (d/q '[:find ?name ?e
                       :where
                       [?e :counter/name ?name]
                       [?e :counter/enabled true]]
                     db)]
    results))

(defn find-counter-by-name
  [conn name]
  (let [db (d/db conn)]
    (ffirst
     (d/q '[:find ?e
            :in $ ?name
            :where
            [?e :counter/name ?name]
            [?e :counter/enabled true]]
          db
          name))))

(defn find-counter-by-id
  [conn id]
  (let [db (d/db conn)]
    (when (number? id)
      (let [entity (d/pull db [:db/id :counter/name :counter/enabled] id)]
        (when (= true (:counter/enabled entity))
          entity)))))

(defn disable-counter!
  [conn id]
  (println "[repo] disable-counter! ->" id)
  (d/transact conn
              {:tx-data [{:db/id id
                          :counter/enabled false
                          :counter/updated-at (java.util.Date.)
                          :counter/action "disable"}]}))
