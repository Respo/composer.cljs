
(ns app.updater.template
  (:require [app.schema :as schema]
            [bisection-key.core :as bisection]
            [bisection-key.util :refer [key-append]]))

(defn append-markup [db op-data sid op-id op-time]
  (let [template-id (:template-id op-data), focused-path (:path op-data)]
    (update-in
     db
     (concat
      [:templates template-id :markup :children]
      (interleave focused-path (repeat :children)))
     (fn [children]
       (let [next-key (key-append children)]
         (assoc children next-key (merge schema/markup {:id next-key, :type :box})))))))

(defn create-mock [db op-data sid op-id op-time]
  (let [template-id (:template-id op-data)
        text (:text op-data)
        new-mock (merge schema/mock {:id op-id, :name text})]
    (assoc-in db [:templates template-id :mocks op-id] new-mock)))

(defn create-template [db op-data sid op-id op-time]
  (let [markup-id "system"
        base-markup (merge schema/markup {:id markup-id, :type :box, :layout :row})
        new-template (merge schema/template {:id op-id, :name op-data, :markup base-markup})]
    (assoc-in db [:templates op-id] new-template)))

(defn remove-mock [db op-data sid op-id op-time]
  (let [template-id (:template-id op-data), mock-id (:mock-id op-data)]
    (update-in db [:templates template-id :mocks] (fn [mocks] (dissoc mocks mock-id)))))

(defn remove-template [db op-data sid op-id op-time]
  (update db :templates (fn [templates] (dissoc templates op-data))))

(defn rename-mock [db op-data sid op-id op-time]
  (assoc-in
   db
   [:templates (:template-id op-data) :mocks (:mock-id op-data) :name]
   (:text op-data)))

(defn rename-template [db op-data sid op-id op-time]
  (let [id (:id op-data), new-name (:name op-data)]
    (update-in db [:templates id] (fn [template] (assoc template :name new-name)))))

(defn update-mock [db op-data sid op-id op-time]
  (let [template-id (:template-id op-data), mock-id (:mock-id op-data), data (:data op-data)]
    (assoc-in db [:templates template-id :mocks mock-id :data] data)))

(defn use-mock [db op-data sid op-id op-time]
  (assoc-in db [:templates (:template-id op-data) :mock-pointer] (:mock-id op-data)))
