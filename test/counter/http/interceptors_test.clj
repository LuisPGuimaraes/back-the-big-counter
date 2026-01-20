(ns counter.http.interceptors-test
  (:require [clojure.test :refer [deftest is testing]]
            [counter.http.interceptors :as interceptors]))

(deftest error-interceptor-test
  (testing "Handles ExceptionInfo with mapped types"
      (let [interceptor (interceptors/error-interceptor)
          context {}
          ex (ex-info "boom" {:type :invalid-name})
          result ((:error interceptor) context ex)]
      (is (= 400 (get-in result [:response :status])))
      (is (= "{\"error\":{\"type\":\"invalid-name\",\"message\":\"boom\"}}"
             (get-in result [:response :body])))))

  (testing "Handles unknown exceptions as 500"
      (let [interceptor (interceptors/error-interceptor)
          context {}
          ex (Exception. "boom")
          result ((:error interceptor) context ex)]
      (is (= 500 (get-in result [:response :status])))
      (is (= "{\"error\":{\"type\":\"internal-server-error\",\"message\":\"internal server error\"}}"
             (get-in result [:response :body]))))))
