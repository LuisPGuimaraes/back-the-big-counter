(ns counter.application.counter-service
  (:require [counter.infra.db.counter-repo :as repo]))

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
