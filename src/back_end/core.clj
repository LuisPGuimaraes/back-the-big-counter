(ns back-end.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [back-end.http :as app-http]))

(defn -main
  [& _args]
  (println "Starting Pedestal server on port 3000.")
  (-> app-http/service
     http/create-server
     http/start))