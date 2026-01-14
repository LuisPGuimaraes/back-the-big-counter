(ns back-end.core
  (:gen-class)
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn health-handler
  [_request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "ok"})

(def routes
  (route/expand-routes
   #{["/health" :get health-handler :route-name :health]}))

(def service
  {::http/routes routes
   ::http/type :jetty
   ::http/port 3000
   ::http/join? true})

(defonce server (atom nil))

(defn start
  []
  (let [srv (-> service http/create-server http/start)]
    (reset! server srv)
    srv))

(defn stop
  []
  (when-let [srv @server]
    (http/stop srv)
    (reset! server nil)))

(defn -main
  [& _args]
  (println "Starting Pedestal server on port 3000.")
  (start))
