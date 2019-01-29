
(ns app.updater.router (:require [app.util :refer [path-with-children]]))

(defn change [db op-data sid op-id op-time] (assoc-in db [:sessions sid :router] op-data))

(defn move-after [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :router :data]
   (fn [pointer-data]
     (let [path (:focused-path pointer-data)]
       (if (empty? path)
         pointer-data
         (let [template-id (:pointer pointer-data)
               children (get-in
                         db
                         (concat
                          [:templates template-id :markup]
                          (path-with-children (butlast path))))
               keys-in-order (vec (sort (keys children)))
               idx (.indexOf keys-in-order (last path))
               next-key (get keys-in-order (inc idx))]
           (if (some? next-key)
             (assoc pointer-data :focused-path (conj (vec (butlast path)) next-key))
             pointer-data)))))))

(defn move-append [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :router :data]
   (fn [pointer-data]
     (let [template-id (:pointer pointer-data)
           path (:focused-path pointer-data)
           children (get-in
                     db
                     (concat [:templates template-id :markup] (path-with-children path)))
           next-key (last (keys children))]
       (println "look" children next-key)
       (if (some? next-key)
         (assoc pointer-data :focused-path (conj (vec path) next-key))
         pointer-data)))))

(defn move-before [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :router :data]
   (fn [pointer-data]
     (let [path (:focused-path pointer-data)]
       (if (empty? path)
         pointer-data
         (let [template-id (:pointer pointer-data)
               children (get-in
                         db
                         (concat
                          [:templates template-id :markup]
                          (path-with-children (butlast path))))
               keys-in-order (vec (sort (keys children)))
               idx (.indexOf keys-in-order (last path))
               next-key (get keys-in-order (dec idx))]
           (if (some? next-key)
             (assoc pointer-data :focused-path (conj (vec (butlast path)) next-key))
             pointer-data)))))))

(defn move-prepend [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :router :data]
   (fn [pointer-data]
     (let [template-id (:pointer pointer-data)
           path (:focused-path pointer-data)
           children (get-in
                     db
                     (concat [:templates template-id :markup] (path-with-children path)))
           next-key (first (keys children))]
       (if (some? next-key)
         (assoc pointer-data :focused-path (conj (vec path) next-key))
         pointer-data)))))

(defn set-focused-mock [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :router :data :focused-mock] op-data))

(defn set-focused-path [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :router :data :focused-path] op-data))

(defn set-pointer [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :router :data :pointer] op-data))

(defn set-tab [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid :router :data :tab] op-data))
