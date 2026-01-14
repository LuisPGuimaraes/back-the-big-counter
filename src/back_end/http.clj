(ns back-end.http
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]))

(defn health-handler
  [_request]
  {:status 200
   :headers {"content-type" "text/plain"}
   :body "ok"})

(def routes
  (route/expand-routes
   (list ["/health" :get health-handler :route-name :health])))

(def service
  {::http/routes routes
   ::http/type :jetty
   ::http/port 3000
   ::http/join? true})
