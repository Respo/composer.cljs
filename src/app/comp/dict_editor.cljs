
(ns app.comp.dict-editor
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> action-> list-> cursor-> button input span div]]
            [clojure.string :as string]
            [app.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [feather.core :refer [comp-i]]))

(defcomp
 comp-dict-editor
 (states dict on-change)
 (let [state (or (:data states) {:draft ""})]
   (div
    {}
    (list->
     {}
     (->> dict
          (map
           (fn [[k v]]
             [k
              (div
               {:style ui/row}
               (<> k)
               (=< 8 nil)
               (<> v)
               (=< 8 nil)
               (span
                {:on-click (fn [e d! m!] (on-change {:type :remove, :key k} m! d!))}
                (comp-i :delete 14 (hsl 200 80 70))))]))))
    (div
     {:style ui/row}
     (input
      {:style ui/input,
       :value (:draft state),
       :on-change (fn [e d! m!] (m! (assoc state :draft (:value e))))})
     (=< 8 nil)
     (button
      {:style ui/button,
       :inner-text "Change",
       :on-click (fn [e d! m!]
         (let [[x1 x2] (string/split (:draft state) ":")]
           (on-change {:type :set, :key (string/trim x1), :value (string/trim x2)} m! d!)
           (m! (assoc state :draft ""))))})))))
