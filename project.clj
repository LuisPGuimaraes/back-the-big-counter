(defproject back-end "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [com.datomic/local "1.0.291"]
                 [cheshire "5.13.0"]
                 [clj-http "3.12.3"]
                 [io.pedestal/pedestal.service "0.6.4"]
                 [io.pedestal/pedestal.jetty "0.6.4"]
                 [prismatic/schema "1.4.1"]
                 [org.clojure/tools.logging "1.3.0"]
                 [org.slf4j/slf4j-simple "2.0.13"]]
  :profiles {:test {:jvm-opts ["-Dorg.slf4j.simpleLogger.defaultLogLevel=off"]}}
  :main counter.core)
