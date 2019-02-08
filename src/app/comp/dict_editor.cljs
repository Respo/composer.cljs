
(ns app.comp.dict-editor
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core
             :refer
             [defcomp <> action-> list-> cursor-> button input span div a]]
            [clojure.string :as string]
            [app.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [feather.core :refer [comp-i]]))

(defcomp
 comp-dict-editor
 (states title dict on-change)
 (let [state (or (:data states) {:draft ""})]
   (div
    {}
    (div
     {:style ui/row-middle}
     (<> title)
     (=< 8 nil)
     (input
      {:style (merge ui/input {:line-height "24px", :height "24px"}),
       :value (:draft state),
       :on-input (fn [e d! m!] (m! (assoc state :draft (:value e)))),
       :on-keydown (fn [e d! m!]
         (if (= 13 (:key-code e))
           (let [[x1 x2] (string/split (:draft state) ":")]
             (on-change {:type :set, :key (string/trim x1), :value (string/trim x2)} m! d!)
             (m! (assoc state :draft "")))))}))
    (list->
     {:style {:padding-left 16}}
     (->> dict
          (map
           (fn [[k v]]
             [k
              (div
               {:style (merge ui/row-middle {:line-height "24px"})}
               (<> k {:color (hsl 0 0 70)})
               (=< 8 nil)
               (<> v)
               (=< 8 nil)
               (span
                {:style {:cursor :pointer},
                 :on-click (fn [e d! m!] (on-change {:type :remove, :key k} m! d!))}
                (comp-i :delete 14 (hsl 200 80 70))))])))))))
