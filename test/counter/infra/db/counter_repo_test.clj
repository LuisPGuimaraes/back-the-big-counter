(ns counter.infra.db.counter-repo-test
  (:require [clojure.test :refer [deftest is testing]]
            [counter.infra.db.counter-repo :as repo]
            [counter.infra.db.datomic.schema :as schema]
            [datomic.client.api :as d]))

(defn- test-conn
  []
  (let [storage-dir (-> (java.nio.file.Files/createTempDirectory
                         "datomic-test"
                         (make-array java.nio.file.attribute.FileAttribute 0))
                        .toFile
                        .getAbsolutePath)
        client (d/client {:server-type :dev-local
                          :system "dev"
                          :storage-dir storage-dir})
        db-name (str "counter-test-" (java.util.UUID/randomUUID))]
    (d/create-database client {:db-name db-name})
    (let [conn (d/connect client {:db-name db-name})]
      (schema/apply-schema! conn)
      conn)))

(defn- resolve-tx
  [tx]
  (if (instance? clojure.lang.IDeref tx)
    @tx
    tx))

(defn- insert-counter!
  [conn name value enabled]
  (resolve-tx
   (d/transact conn
               {:tx-data [{:counter/name name
                           :counter/value value
                           :counter/enabled enabled
                           :counter/action "increment"
                           :counter/updated-at (repo/now)}]}))
  (let [db (d/db conn)
        id (ffirst (d/q '[:find ?e
                          :in $ ?name
                          :where
                          [?e :counter/name ?name]]
                        db
                        name))]
    id))

(deftest create-counter-test
  (testing "Creates counter and returns id"
    (with-redefs [d/transact (fn [_ _]
                               (future {:db-after :fake-db}))
                  repo/find-enabled-counter-by-name (fn [db name]
                                                      (is (= :fake-db db))
                                                      (is (= "Test" name))
                                                      1001)]
      (is (= {:id 1001 :name "Test"}
             (repo/create-counter! :fake-conn "Test"))))))

(deftest find-counter-by-id-test
  (testing "Finds counter by id"
    (let [conn (test-conn)
          id (insert-counter! conn "Test" 0 true)
          db (d/db conn)
          entity (repo/find-counter-by-id db id)]
      (is (= "Test" (:counter/name entity)))
      (is (= 0 (:counter/value entity)))
      (is (= true (:counter/enabled entity))))))

(deftest find-enabled-counter-by-name-test
  (testing "Finds enabled counter id by name"
    (let [conn (test-conn)
          id (insert-counter! conn "Beta" 0 true)
          _ (insert-counter! conn "Beta-Person2" 0 false)
          db (d/db conn)
          found-id (repo/find-enabled-counter-by-name db "Beta")]
      (is (= id found-id)))))

(deftest list-enabled-counters-test
  (testing "Lists only enabled counters"
    (let [conn (test-conn)
          _ (insert-counter! conn "Beatriz" 0 true)
          _ (insert-counter! conn "Teresa" 0 true)
          _ (insert-counter! conn "Person1" 0 true)
          _ (insert-counter! conn "Person2" 0 false)
          db (d/db conn)
          counters (repo/list-enabled-counters db)
          names (set (map :counter/name counters))]
      (is (contains? names "Beatriz"))
      (is (contains? names "Teresa"))
      (is (contains? names "Person1"))
      (is (not (contains? names "Person2")))
      (is (= 3 (count counters))))))

(deftest save-and-increment-test
  (testing "Saves and increments counter value"
    (let [conn (test-conn)
          id (insert-counter! conn "Zeta" 0 true)
          _ (repo/save-count! conn id 5 "manual")
          _ (repo/increment-by! conn id 3)
          db (d/db conn)
          entity (repo/find-counter-by-id db id)]
      (is (= 8 (:counter/value entity))))))
