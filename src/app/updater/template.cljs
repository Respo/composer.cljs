
(ns app.updater.template (:require [app.schema :as schema]))

(defn create-template [db op-data sid op-id op-time]
  (let [base-markup (merge schema/markup {:id op-id, :type :box, :layout :row})
        new-template (merge schema/template {:id op-id, :name op-data, :markup base-markup})]
    (assoc-in db [:templates op-id] new-template)))
