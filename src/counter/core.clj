(ns counter.core
  (:require [counter.system :as system])
  (:gen-class))

(defn -main
  [& _args]
  (system/start))
