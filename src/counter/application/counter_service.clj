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
