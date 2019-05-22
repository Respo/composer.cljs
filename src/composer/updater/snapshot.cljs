
(ns composer.updater.snapshot )

(defn reset-version [db op-data sid op-id op-time]
  (let [upstream op-data]
    (if (= (:templates db) (:saved-templates db))
      (-> db
          (assoc :templates (:templates upstream))
          (assoc :saved-templates (:templates upstream))
          (assoc :settings (:settings upstream)))
      (update
       db
       :sessions
       (fn [sessions]
         (->> sessions
              (map
               (fn [[k session]]
                 [k
                  (update
                   session
                   :messages
                   (fn [messages]
                     (assoc
                      messages
                      op-id
                      {:id op-id,
                       :text (str
                              "Changes on upstream while these is a draft... Need to handle by hand!")})))]))
              (into {})))))))
