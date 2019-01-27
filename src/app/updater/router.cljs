
(ns app.updater.router )

(defn change [db op-data sid op-id op-time] (assoc-in db [:sessions sid :router] op-data))

(defn set-pointer [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :router :data :pointer] op-data))

(defn set-tab [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :router :data :tab] op-data))
