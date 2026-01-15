(ns counter.http.routes
  (:require [counter.http.handlers :as handlers]
            [io.pedestal.http.route :as route]))

(def routes
  (route/expand-routes
   #{["/health" :get handlers/health-handler :route-name :health]}))
