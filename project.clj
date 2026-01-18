(defproject back-end "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [com.datomic/local "1.0.291"]
                 [cheshire "5.13.0"]
                 [io.pedestal/pedestal.service "0.6.4"]
                 [io.pedestal/pedestal.jetty "0.6.4"]]
  :main counter.core)
