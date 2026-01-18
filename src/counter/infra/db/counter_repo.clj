(ns counter.infra.db.counter-repo
  (:require [datomic.client.api :as d]))

(defn- latest-count-row
  [db]
  (->> (d/q '[:find ?value ?updated
              :where
              [?e :counter/value ?value]
              [?e :counter/updated-at ?updated]]
            db)
       (sort-by second)
       last))


(defn save-count!
  [conn value]
  (println "[repo] save-count! ->" value)
  (d/transact conn
              {:tx-data [{:counter/value value
                          :counter/updated-at (java.util.Date.)}]})
  value)

(defn get-count
  [conn]
  (let [db (d/db conn)
        latest (latest-count-row db)]
    (if latest
      (let [value (first latest)]
        (println "[repo] get-count ->" value)
        value)
      (do
        (println "[repo] get-count -> not found, creating zero")
        (save-count! conn 0)))))

(defn increment!
  [conn]
  (let [current (get-count conn)
        next (inc current)]
    (save-count! conn next)))

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
