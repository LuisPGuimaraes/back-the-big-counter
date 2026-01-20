(ns counter.http.schemas
  (:require [counter.errors :as errors]
            [schema.core :as s]))

(def Id (s/constrained s/Num integer? 'integer))

(def GetCountQuery
  {:id Id})

(def IncrementBody
  {:counter-id Id
   :increment-value s/Num})

(def ResetBody
  {:counter-id Id})

(def CreateCounterBody
  {:name (s/constrained s/Str (complement clojure.string/blank?) 'non-blank)})

(def CountResponse
  {:count Id})

(def Counter
  {:id Id
   :name s/Str
   :value Id})

(def CounterResponse
  {:id Id
   :name s/Str})

(def CountersResponse
  {:counters [Counter]})

(defn validate-request!
  [schema value error-type]
  (try
    (s/validate schema value)
    value
    (catch clojure.lang.ExceptionInfo ex
      (throw (errors/error-info error-type {:schema-error (:error (ex-data ex))})))))

(defn validate-response!
  [schema value]
  (try
    (s/validate schema value)
    value
    (catch clojure.lang.ExceptionInfo _
      (throw (RuntimeException. "invalid response")))))
