
(ns app.updater.router (:require [app.util :refer [path-with-children]]))

(defn change [db op-data sid op-id op-time] (assoc-in db [:sessions sid :router] op-data))

(defn move-after [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :focus-to]
   (fn [focus-to]
     (let [path (:path focus-to)]
       (if (empty? path)
         focus-to
         (let [template-id (:template-id focus-to)
               children (get-in
                         db
                         (concat
                          [:templates template-id :markup]
                          (path-with-children (butlast path))))
               keys-in-order (vec (sort (keys children)))
               idx (.indexOf keys-in-order (last path))
               next-key (get keys-in-order (inc idx))]
           (if (some? next-key)
             (assoc focus-to :path (conj (vec (butlast path)) next-key))
             focus-to)))))))

(defn move-append [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :focus-to]
   (fn [focus-to]
     (let [template-id (:template-id focus-to)
           path (:path focus-to)
           children (get-in
                     db
                     (concat [:templates template-id :markup] (path-with-children path)))
           next-key (last (sort (keys children)))]
       (if (some? next-key) (assoc focus-to :path (conj (vec path) next-key)) focus-to)))))

(defn move-before [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :focus-to]
   (fn [focus-to]
     (let [path (:path focus-to)]
       (if (empty? path)
         focus-to
         (let [template-id (:template-id focus-to)
               children (get-in
                         db
                         (concat
                          [:templates template-id :markup]
                          (path-with-children (butlast path))))
               keys-in-order (vec (sort (keys children)))
               idx (.indexOf keys-in-order (last path))
               next-key (get keys-in-order (dec idx))]
           (if (some? next-key)
             (assoc focus-to :path (conj (vec (butlast path)) next-key))
             focus-to)))))))

(defn move-prepend [db op-data sid op-id op-time]
  (update-in
   db
   [:sessions sid :focus-to]
   (fn [focus-to]
     (let [template-id (:template-id focus-to)
           path (:path focus-to)
           children (get-in
                     db
                     (concat [:templates template-id :markup] (path-with-children path)))
           next-key (first (sort (keys children)))]
       (if (some? next-key) (assoc focus-to :path (conj (vec path) next-key)) focus-to)))))
