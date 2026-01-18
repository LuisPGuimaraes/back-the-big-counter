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
