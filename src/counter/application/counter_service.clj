(ns counter.application.counter-service
  (:require [clojure.string :as string]
            [counter.infra.db.counter-repo :as repo]))

(defn get-count
  [conn]
  (println "[service] get-count called")
  (repo/get-count conn))

(defn increment!
  [conn]
  (println "[service] increment! called")
  (repo/increment! conn))

(defn reset!
  [conn]
  (println "[service] reset! called")
  (repo/save-count! conn 0))

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
  (when (string/blank? name)
    (throw (ex-info "name is required" {})))
  (when (repo/find-counter-by-name conn name)
    (throw (ex-info "counter already exists" {:type :counter-already-exists})))
  (repo/create-counter! conn name))

(defn delete-counter
  [conn id]
  (println "[service] delete-counter called")
  (when (nil? id)
    (throw (ex-info "id is required" {})))
  (let [counter (repo/find-counter-by-id conn id)]
    (when (nil? counter)
      (throw (ex-info "counter not found" {:type :counter-not-found})))
    (repo/disable-counter! conn id)))
