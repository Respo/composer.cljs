
(ns composer.updater.settings )

(defn add-color [db op-data sid op-id op-time]
  (assoc-in
   db
   [:settings :colors op-id]
   {:id op-id, :name (:name op-data), :color (:color op-data), :group (:group op-data)}))

(defn remove-color [db op-data sid op-id op-time]
  (update-in db [:settings :colors] (fn [colors] (dissoc colors op-data))))

(defn update-color [db op-data sid op-id op-time]
  (assoc-in db [:settings :colors (:id op-data) :color] (:color op-data)))
