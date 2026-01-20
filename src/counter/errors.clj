(ns counter.errors)

(def error-definitions
  {:invalid-increment-type {:status 400 :message "increment value must be a number"}
   :invalid-increment {:status 400 :message "increment value must be greater than 0"}
   :invalid-name {:status 400 :message "name is required"}
   :counter-already-exists {:status 409 :message "counter with this name already exists"}
   :missing-id {:status 400 :message "id is required"}
   :invalid-id {:status 400 :message "id is invalid"}
   :counter-not-found {:status 404 :message "counter not found"}})

(def default-client-error
  {:status 400 :message "bad request"})

(defn error-definition
  [type]
  (get error-definitions type default-client-error))

(defn error-info
  ([type]
   (error-info type nil))
  ([type data]
   (let [{:keys [message]} (error-definition type)]
     (ex-info message (merge {:type type} data)))))
