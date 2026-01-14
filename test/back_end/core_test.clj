(ns back-end.core-test
  (:require [clojure.test :refer :all]
            [back-end.http :as app-http]))

(deftest health-handler-test
  (testing "When Health endpoint returns ok"
    (let [resp (app-http/health-handler {:request-method :get})]
      (is (= 200 (:status resp)))
      (is (= "ok" (:body resp)))
      (is (= "text/plain" (get-in resp [:headers "content-type"]))))))
