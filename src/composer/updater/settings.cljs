
(ns composer.updater.settings (:require [composer.schema :as schema]))

(defn add-color [db op-data sid op-id op-time]
  (update-in
   db
   [:settings :color-groups (:group-id op-data)]
   (fn [group]
     (assoc-in
      group
      [:colors op-id]
      (merge schema/color {:id op-id, :name (:name op-data), :color (:color op-data)})))))

(defn add-color-group [db op-data sid op-id op-time]
  (assoc-in
   db
   [:settings :color-groups op-id]
   (merge schema/color-group {:id op-id, :name op-data})))

(defn create-preset [db op-data sid op-id op-time]
  (assoc-in db [:settings :presets op-id] (merge schema/preset {:id op-id, :name op-data})))

(defn remove-color [db op-data sid op-id op-time]
  (update-in
   db
   [:settings :color-groups (:group-id op-data) :colors]
   (fn [colors] (dissoc colors (:id op-data)))))

(defn remove-color-group [db op-data sid op-id op-time]
  (update-in db [:settings :color-groups] (fn [groups] (dissoc groups op-data))))

(defn remove-preset [db op-data sid op-id op-time]
  (update-in db [:settings :presets] (fn [presets] (dissoc presets op-data))))

(defn rename-color-group [db op-data sid op-id op-time]
  (assoc-in db [:settings :color-groups (:id op-data) :name] (:name op-data)))

(defn rename-preset [db op-data sid op-id op-time]
  (assoc-in db [:settings :presets (:id op-data) :name] (:name op-data)))

(defn update-color [db op-data sid op-id op-time]
  (update-in
   db
   [:settings :color-groups (:group-id op-data) :colors (:id op-data)]
   (fn [color] (merge color op-data))))

(defn update-preset [db op-data sid op-id op-time]
  (assoc-in db [:settings :presets (:id op-data) :style] (:style op-data)))
