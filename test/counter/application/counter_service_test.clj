(ns counter.application.counter-service-test
  (:require [clojure.test :refer [deftest is testing]]
            [counter.application.counter-service :as service]
            [counter.infra.db.counter-repo :as repo]
            [datomic.client.api :as d]))

(deftest get-count-test
  (testing "When Get Count is called"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db counter-id]
                                            (is (= :fake-db db))
                                            (is (= 10 counter-id))
                                            {:counter/value 7})]
      (is (= 7 (service/get-count :fake-conn 10))))))

(deftest increment-test
  (testing "When counter does not exist"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db _]
                                            (is (= :fake-db db))
                                            nil)]
      (try
        (service/increment! :fake-conn 1 1)
        (is false "Expected exception for missing counter")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :counter-not-found (:type (ex-data ex))))))))

  (testing "When increment value is invalid"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db _]
                                            (is (= :fake-db db))
                                            {:db/id 1})]
      (try
        (service/increment! :fake-conn 1 0)
        (is false "Expected exception for invalid increment")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :invalid-increment (:type (ex-data ex))))))))

  (testing "When increment succeeds"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db _]
                                            (is (= :fake-db db))
                                            {:db/id 1})
                  repo/increment-by! (fn [_ counter-id increment-value]
                                       (is (= 1 counter-id))
                                       (is (= 2 increment-value))
                                       9)]
      (is (= 9 (service/increment! :fake-conn 1 2))))))

(deftest reset-test
  (testing "When Reset is called"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db _]
                                            (is (= :fake-db db))
                                            {:db/id 1})
                  repo/save-count! (fn [_ id value action]
                                     (is (= 1 id))
                                     (is (= 0 value))
                                     (is (= "reset" action))
                                     0)]
      (is (= 0 (service/reset-counter! :fake-conn 1))))))

(deftest get-counters-test
  (testing "When Get Counters is called"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/list-enabled-counters (fn [db]
                                               (is (= :fake-db db))
                                               [{:db/id 1 :counter/name "Main" :counter/value 2}
                                                {:db/id 2 :counter/name "Aux" :counter/value 7}])]
      (is (= [{:id 1 :name "Main" :value 2}
              {:id 2 :name "Aux" :value 7}]
             (service/get-counters :fake-conn))))))

(deftest create-counter-test
  (testing "When counter already exists"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-enabled-counter-by-name (fn [db _]
                                                      (is (= :fake-db db))
                                                      1)]
      (try
        (service/create-counter :fake-conn "Main")
        (is false "Expected exception for duplicate counter")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :counter-already-exists (:type (ex-data ex))))))))

  (testing "When counter is created"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-enabled-counter-by-name (fn [db _]
                                                      (is (= :fake-db db))
                                                      nil)
                  repo/create-counter! (fn [_ name]
                                         (is (= "Main" name))
                                         {:id 1 :name name})]
      (is (= {:id 1 :name "Main"}
             (service/create-counter :fake-conn "Main"))))))

(deftest delete-counter-test
  (testing "When counter does not exist"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db _]
                                            (is (= :fake-db db))
                                            nil)]
      (try
        (service/delete-counter :fake-conn 1)
        (is false "Expected exception for missing counter")
        (catch clojure.lang.ExceptionInfo ex
          (is (= :counter-not-found (:type (ex-data ex))))))))

  (testing "When counter is deleted"
    (with-redefs [d/db (fn [_] :fake-db)
                  repo/find-counter-by-id (fn [db _]
                                            (is (= :fake-db db))
                                            {:db/id 1})
                  repo/disable-counter! (fn [_ id]
                                          (is (= 1 id))
                                          nil)]
      (is (= nil (service/delete-counter :fake-conn 1))))))
