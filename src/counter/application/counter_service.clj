(ns counter.application.counter-service
  (:require [counter.errors :as errors]
            [counter.infra.db.counter-repo :as repo]
            [datomic.client.api :as d]))

(defn get-count
  [conn counter-id]
  (println "[service] get-count called")
  (let [db (d/db conn)] 
    (:counter/value (repo/find-counter-by-id db counter-id))))

(defn increment!
  [conn counter-id increment-value]
  (let [db (d/db conn)]
    (when (nil? (repo/find-counter-by-id db counter-id))
      (throw (errors/error-info :counter-not-found))))
  (when (<= increment-value 0)
    (throw (errors/error-info :invalid-increment)))
  (println "[service] increment! called with value:" increment-value)
  (repo/increment-by! conn counter-id increment-value))

(defn reset-counter!
  [conn counter-id]
  (println "[service] reset! called")
  (let [db (d/db conn)]
    (when (nil? (repo/find-counter-by-id db counter-id))
      (throw (errors/error-info :counter-not-found))))
  (repo/save-count! conn counter-id 0 "reset"))

(defn get-counters
  [conn]
  (println "[service] get-counters called")
  (let [db (d/db conn)]
    (map (fn [counter]
           {:id (:db/id counter)
            :name (:counter/name counter)
            :value (:counter/value counter)})
         (repo/list-enabled-counters db))))

(defn create-counter
  [conn name]
  (println "[service] create-counter called")
  (let [db (d/db conn)]
    (when (repo/find-enabled-counter-by-name db name)
      (throw (errors/error-info :counter-already-exists))))
  (repo/create-counter! conn name))

(defn delete-counter
  [conn id]
  (println "[service] delete-counter called")
  (let [db (d/db conn)
        counter (repo/find-counter-by-id db id)]
    (when (nil? counter)
      (throw (errors/error-info :counter-not-found)))
    (repo/disable-counter! conn id)))
