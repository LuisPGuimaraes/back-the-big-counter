(ns counter.application.counter-service
  (:require [clojure.string :as string]
            [counter.infra.db.counter-repo :as repo]))

(defn get-count
  [conn counter-id]
  (println "[service] get-count called")
  (repo/get-count-by-id conn counter-id))

(defn increment!
  [conn counter-id increment-value]
  (when (nil? (repo/find-counter-by-id conn counter-id))
    (throw (ex-info "counter not found" {:type :counter-not-found})))
  (when (<= increment-value 0)
    (throw (ex-info "increment value must be greater than 0" {:type :invalid-increment})))
  (println "[service] increment! called with value:" increment-value)
  (repo/increment-by! conn counter-id increment-value))

(defn reset!
  [conn counter-id]
  (println "[service] reset! called")
  (when (nil? (repo/find-counter-by-id conn counter-id))
    (throw (ex-info "counter not found" {:type :counter-not-found})))
  (repo/save-count! conn 0 counter-id))

(defn get-counters
  [conn]
  (println "[service] get-counters called")
  (map (fn [[name id]]
         {:name name
          :id id})
       (repo/list-counters conn)))

(defn create-counter
  [conn name]
  (println "[service] create-counter called")
  (when (repo/find-counter-by-name conn name)
    (throw (ex-info "counter already exists" {:type :counter-already-exists})))
  (repo/create-counter! conn name))

(defn delete-counter
  [conn id]
  (println "[service] delete-counter called")
  (let [counter (repo/find-counter-by-id conn id)]
    (when (nil? counter)
      (throw (ex-info "counter not found" {:type :counter-not-found})))
    (repo/disable-counter! conn id)))
