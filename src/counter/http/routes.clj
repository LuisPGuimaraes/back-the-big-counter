(ns counter.http.routes
  (:require [counter.http.handlers :as handlers]
            [io.pedestal.http.route :as route]))

(def routes
  (route/expand-routes
   #{["/health" :get handlers/health-handler :route-name :health]
     ["/count" :get handlers/get-count :route-name :get-count]
     ["/count/increment" :post handlers/increment-count :route-name :increment-count]
     ["/count/reset" :post handlers/reset-count :route-name :reset-count]}))
