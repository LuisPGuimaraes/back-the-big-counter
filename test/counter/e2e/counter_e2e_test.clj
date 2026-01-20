(ns counter.e2e.counter-e2e-test
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [clojure.test :refer [deftest is testing use-fixtures]]
            [counter.infra.db.datomic.datomic :as datomic]
            [counter.system :as system]
            [io.pedestal.http :as http-server]))

(def ^:dynamic *base-url* nil)

(defn- temp-dir []
  (-> (java.nio.file.Files/createTempDirectory
       "datomic-e2e-test"
       (make-array java.nio.file.attribute.FileAttribute 0))
      .toFile
      .getAbsolutePath))

(defn- pick-port []
  (with-open [socket (java.net.ServerSocket. 0)]
    (.getLocalPort socket)))

(defn- wait-for-server [base-url]
  (loop [n 25]
    (let [resp (try
                 (http/get (str base-url "/health")
                           {:throw-exceptions false})
                 (catch Exception _
                   nil))]
      (if (and resp (= 200 (:status resp)))
        true
        (when (pos? n)
          (Thread/sleep 100)
          (recur (dec n)))))))

(defn- with-system
  [f]
  (let [storage-dir (temp-dir)
        db-name (str "counter-test-" (java.util.UUID/randomUUID))
        port (pick-port)
        base-url (str "http://localhost:" port)]
    (with-redefs [datomic/storage-dir (fn [] storage-dir)
                  datomic/db-name (fn [] db-name)
                  system/service (assoc system/service ::http-server/port port)]
      (let [sys (system/create-system)
            started (update sys :server http-server/start)]
        (try
          (when-not (wait-for-server base-url)
            (throw (ex-info "Server did not start" {:port port})))
          (binding [*base-url* base-url]
            (f))
          (finally
            (http-server/stop (:server started))))))))

(use-fixtures :once with-system)

(defn- body-key
  [resp k]
  (or (get-in resp [:body k])
      (get-in resp [:body (name k)])))

(defn- create-counter!
  [name]
  (http/post (str *base-url* "/counter/create")
             {:content-type :json
              :accept :json
              :as :json
              :throw-exceptions false
              :body (json/generate-string {:name name})}))

(deftest health-e2e-test
  (testing "Health endpoint"
    (let [resp (http/get (str *base-url* "/health")
                         {:throw-exceptions false})]
      (is (= 200 (:status resp)))
      (is (= "ok" (:body resp))))))

(deftest create-counter-e2e-test
  (testing "Create counter"
    (let [resp (create-counter! "Main")]
      (is (= 201 (:status resp)))
      (is (integer? (body-key resp :id)))
      (is (= "Main" (body-key resp :name))))))

(deftest get-counters-e2e-test
  (testing "Get counters includes newly created counter"
    (let [_ (create-counter! "List-A")
          resp (http/get (str *base-url* "/counter")
                         {:accept :json
                          :as :json
                          :throw-exceptions false})
          counters (or (body-key resp :counters) [])
          names (set (map #(or (:name %) (get % "name")) counters))]
      (is (= 200 (:status resp)))
      (is (contains? names "List-A")))))

(deftest get-count-e2e-test
  (testing "Get count for created counter"
    (let [create-resp (create-counter! "Count-A")
          id (body-key create-resp :id)
          resp (http/get (str *base-url* "/count")
                         {:query-params {:id id}
                          :accept :json
                          :as :json
                          :throw-exceptions false})]
      (is (= 200 (:status resp)))
      (is (= 0 (body-key resp :count))))))

(deftest increment-count-e2e-test
  (testing "Increment counter"
    (let [create-resp (create-counter! "Inc-A")
          id (body-key create-resp :id)
          resp (http/post (str *base-url* "/count/increment")
                          {:content-type :json
                           :accept :json
                           :as :json
                           :throw-exceptions false
                           :body (json/generate-string {:counter-id id
                                                        :increment-value 3})})]
      (is (= 200 (:status resp)))
      (is (= 3 (body-key resp :count))))))

(deftest reset-count-e2e-test
  (testing "Reset counter"
    (let [create-resp (create-counter! "Reset-A")
          id (body-key create-resp :id)
          _ (http/post (str *base-url* "/count/increment")
                       {:content-type :json
                        :accept :json
                        :as :json
                        :throw-exceptions false
                        :body (json/generate-string {:counter-id id
                                                     :increment-value 2})})
          resp (http/post (str *base-url* "/count/reset")
                          {:content-type :json
                           :accept :json
                           :as :json
                           :throw-exceptions false
                           :body (json/generate-string {:counter-id id})})]
      (is (= 200 (:status resp)))
      (is (= 0 (body-key resp :count))))))

(deftest delete-counter-e2e-test
  (testing "Delete counter"
    (let [create-resp (create-counter! "Delete-A")
          id (body-key create-resp :id)
          delete-resp (http/delete (str *base-url* "/counter")
                                   {:query-params {:id id}
                                    :throw-exceptions false})
          list-resp (http/get (str *base-url* "/counter")
                              {:accept :json
                               :as :json
                               :throw-exceptions false})
          counters (or (body-key list-resp :counters) [])
          names (set (map #(or (:name %) (get % "name")) counters))]
      (is (= 204 (:status delete-resp)))
      (is (not (contains? names "Delete-A"))))))
