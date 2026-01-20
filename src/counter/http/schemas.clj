(ns counter.http.schemas
  (:require [schema.core :as s]))

(def Id s/Int)

(def GetCountQuery
  {:id Id})

(def IncrementBody
  {:counter-id Id
   :increment-value s/Num})

(def ResetBody
  {:counter-id Id})

(def CreateCounterBody
  {:name s/Str})

(def CountResponse
  {:count s/Int})

(def Counter
  {:id s/Int
   :name s/Str
   :value s/Int})

(def CounterResponse
  {:id s/Int
   :name s/Str})

(def CountersResponse
  {:counters [Counter]})

(defn validate-request!
  [schema value error-type]
  (try
    (s/validate schema value)
    value
    (catch clojure.lang.ExceptionInfo ex
      (throw (ex-info "invalid request" {:type error-type
                                          :schema-error (:error (ex-data ex))})))))

(defn validate-response!
  [schema value]
  (try
    (s/validate schema value)
    value
    (catch clojure.lang.ExceptionInfo _
      (throw (RuntimeException. "invalid response")))))
