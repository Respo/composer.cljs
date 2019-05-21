
(ns composer.updater.session (:require [composer.schema :as schema]))

(defn connect [db op-data sid op-id op-time]
  (assoc-in db [:sessions sid] (merge schema/session {:id sid})))

(defn copy-markup [db op-data sid op-id op-time]
  (let [template-id (:template-id op-data)
        focused-path (:path op-data)
        root-markup (get-in db [:templates template-id :markup])
        markup (get-in root-markup (interleave (repeat :children) focused-path))]
    (println "copied" markup)
    (assoc-in db [:sessions sid :copied-markup] markup)))

(defn disconnect [db op-data sid op-id op-time]
  (update db :sessions (fn [session] (dissoc session sid))))

(defn focus-to [db op-data sid op-id op-time]
  (update-in db [:sessions sid :focus-to] (fn [settings] (merge settings op-data))))

(defn jump-template [db op-data sid op-id op-time]
  (let [target (->> (vals (:templates db))
                    (filter (fn [template] (= (str "\"" (:name template)) op-data)))
                    (first))]
    (if (some? target)
      (update-in
       db
       [:sessions sid :focus-to]
       (fn [settings] (merge settings {:template-id (:id target), :path [], :mock-id nil})))
      (update-in
       db
       [:sessions sid :messages]
       (fn [messages]
         (assoc messages op-id {:id op-id, :text (str "Unknown template: " op-data)}))))))

(defn paste-markup [db op-data sid op-id op-time]
  (let [template-id (:template-id op-data)
        focused-path (:path op-data)
        the-markup (get-in db [:sessions sid :copied-markup])]
    (if (nil? the-markup)
      db
      (assoc-in
       db
       (concat
        [:templates template-id :markup]
        (interleave (repeat :children) focused-path))
       the-markup))))

(defn remove-message [db op-data sid op-id op-time]
  (update-in db [:sessions sid :messages] (fn [messages] (dissoc messages (:id op-data)))))

(defn toggle-shadows [db op-data sid op-id op-data]
  (update-in db [:sessions sid :shadows?] not))
