(ns counter.http.handlers-test
  (:require [clojure.test :refer :all]
            [counter.http.handlers :as app-http]))

(deftest health-handler-test
  (testing "When Health endpoint returns ok"
    (let [resp (app-http/health-handler {:request-method :get})]
      (is (= 200 (:status resp)))
      (is (= "ok" (:body resp)))
      (is (= "text/plain" (get-in resp [:headers "content-type"]))))))

(deftest get-count-test
  (testing "When Get Count endpoint is called"
    (testing "When returns a count value"
      (let [resp (app-http/get-count {:request-method :get})]
        (is (= 200 (:status resp)))
        (is (= "{\"count\": 42}" (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))

    (testing "When returns same error"
      (let [resp (app-http/get-count {:request-method :get})]
        (is (= 400 (:status resp)))
        (is (= "{\"error\": \"error while getting count\"}" (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))))

(deftest increment-count-test
  (testing "When Increment Count endpoint is called"
    (testing "When returns an incremented count value"
      (let [resp (app-http/increment-count {:request-method :post})]
        (is (= 200 (:status resp)))
        (is (= "{\"count\": 43}" (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))

    (testing "When returns same error"
      (let [resp (app-http/increment-count {:request-method :post})]
        (is (= 400 (:status resp)))
        (is (= "{\"error\": \"error while incrementing count\"}" (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))))

(deftest reset-count-test
  (testing "When Reset Count endpoint is called"
    (testing "When returns a reset count value"
      (let [resp (app-http/reset-count {:request-method :post})]
        (is (= 200 (:status resp)))
        (is (= "{\"count\": 0}" (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))

    (testing "When returns same error"
      (let [resp (app-http/reset-count {:request-method :post})]
        (is (= 400 (:status resp)))
        (is (= "{\"error\": \"error while resetting count\"}" (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))))
