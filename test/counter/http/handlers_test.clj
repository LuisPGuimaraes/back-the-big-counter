(ns counter.http.handlers-test
  (:require [clojure.test :refer [deftest is testing]]
            [counter.application.counter-service :as service]
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
      (with-redefs [service/get-count (fn [_ counter-id]
                                        (is (= 42 counter-id))
                                        42)]
        (let [resp (app-http/get-count {:request-method :get
                                        :db/conn :fake
                                        :query-params {:id "42"}})]
          (is (= 200 (:status resp)))
          (is (= "{\"count\":42}" (:body resp)))
          (is (= "application/json" (get-in resp [:headers "content-type"]))))))

    (testing "When missing id"
      (try
        (app-http/get-count {:request-method :get
                             :db/conn :fake})
        (is false "Expected exception for missing id")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :missing-id (:type (ex-data ex)))))))))

(deftest increment-count-test
  (testing "When Increment Count endpoint is called"
    (testing "When returns an incremented count value"
      (with-redefs [service/increment! (fn [_ counter-id increment-value]
                                         (is (= 10 counter-id))
                                         (is (= 3 increment-value))
                                         43)]
        (let [resp (app-http/increment-count {:request-method :post
                                              :db/conn :fake
                                              :json-params {:counter-id "10"
                                                            :increment-value 3}})]
          (is (= 200 (:status resp)))
          (is (= "{\"count\":43}" (:body resp)))
          (is (= "application/json" (get-in resp [:headers "content-type"]))))))

    (testing "When missing id"
      (try
        (app-http/increment-count {:request-method :post
                                   :db/conn :fake
                                   :json-params {:increment-value 3}})
        (is false "Expected exception for missing id")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :missing-id (:type (ex-data ex)))))))

    (testing "When increment value is invalid"
      (try
        (app-http/increment-count {:request-method :post
                                   :db/conn :fake
                                   :json-params {:counter-id "10"
                                                 :increment-value "foo"}})
        (is false "Expected exception for invalid increment")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :invalid-increment-type (:type (ex-data ex)))))))))

(deftest reset-count-test
  (testing "When Reset Count endpoint is called"
    (testing "When returns a reset count value"
      (with-redefs [service/reset-counter! (fn [_ counter-id]
                                     (is (= 5 counter-id))
                                     0)]
        (let [resp (app-http/reset-count {:request-method :post
                                          :db/conn :fake
                                          :json-params {:counter-id "5"}})]
          (is (= 200 (:status resp)))
          (is (= "{\"count\":0}" (:body resp)))
          (is (= "application/json" (get-in resp [:headers "content-type"]))))))))

(deftest get-counters-test
  (testing "When Get Counters endpoint is called"
    (with-redefs [service/get-counters (fn [_]
                                         [{:id 1 :name "A" :value 2}
                                          {:id 2 :name "B" :value 7}])]
      (let [resp (app-http/get-counters {:request-method :get
                                         :db/conn :fake})]
        (is (= 200 (:status resp)))
        (is (= "{\"counters\":[{\"id\":1,\"name\":\"A\",\"value\":2},{\"id\":2,\"name\":\"B\",\"value\":7}]}"
               (:body resp)))
        (is (= "application/json" (get-in resp [:headers "content-type"])))))))

(deftest create-counter-test
  (testing "When Create Counter endpoint is called"
    (testing "When returns created counter"
      (with-redefs [service/create-counter (fn [_ name]
                                             (is (= "Main" name))
                                             {:id 1 :name name})]
        (let [resp (app-http/create-counter {:request-method :post
                                             :db/conn :fake
                                             :json-params {:name "Main"}})]
          (is (= 201 (:status resp)))
          (is (= "{\"id\":1,\"name\":\"Main\"}" (:body resp)))
          (is (= "application/json" (get-in resp [:headers "content-type"]))))))

    (testing "When name is blank"
      (try
        (app-http/create-counter {:request-method :post
                                  :db/conn :fake
                                  :json-params {:name "  "}})
        (is false "Expected exception for invalid name")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :invalid-name (:type (ex-data ex)))))))))

(deftest delete-counter-test
  (testing "When Delete Counter endpoint is called"
    (testing "When deletes with id"
      (with-redefs [service/delete-counter (fn [_ id]
                                             (is (= 10 id))
                                             nil)]
        (let [resp (app-http/delete-counter {:request-method :delete
                                             :db/conn :fake
                                             :query-params {:id "10"}})]
          (is (= 204 (:status resp)))
          (is (= "" (:body resp)))
          (is (= "application/json" (get-in resp [:headers "content-type"]))))))

    (testing "When missing id"
      (try
        (app-http/delete-counter {:request-method :delete
                                  :db/conn :fake})
        (is false "Expected exception for missing id")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :missing-id (:type (ex-data ex)))))))))
