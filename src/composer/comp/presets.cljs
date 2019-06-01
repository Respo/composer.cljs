
(ns composer.comp.presets
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp <> action-> cursor-> list-> span div]]
            [composer.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [feather.core :refer [comp-i]]
            [clojure.set :refer [difference]]
            [composer.style :as style]))

(defcomp
 comp-preset
 (preset selected? on-click)
 (div
  {:style (merge
           {:padding "0 10px",
            :margin 2,
            :cursor :pointer,
            :background-color (hsl 200 80 84),
            :color :white,
            :border-radius "4px",
            :line-height "24px",
            :display :inline-block}
           (if selected? {:background-color (hsl 200 80 60)})),
   :on-click on-click}
  (<> (or (:name preset) preset))))

(defcomp
 comp-presets-picker
 (states presets all-presets template-id path)
 (let [handle-op (fn [op preset d!]
                   (d!
                    :template/node-preset
                    {:template-id template-id, :path path, :op op, :value preset}))]
   (cursor->
    :picker
    comp-popup
    states
    {:trigger (comp-i :edit 14 (hsl 200 80 50)), :style {:display :inline-block}}
    (fn [toggle!]
      (div
       {:style {:width 320}}
       (div {} (<> "Selected"))
       (if (empty? presets)
         (<> "Empty" {:font-family ui/font-fancy, :color (hsl 0 0 70)})
         (list->
          {}
          (->> presets
               (map
                (fn [preset-id]
                  (let [preset (get all-presets preset-id)]
                    [preset-id
                     (comp-preset
                      (or preset preset-id)
                      true
                      (fn [e d! m!] (handle-op :remove (:id preset) d!)))]))))))
       (div {} (<> "Others"))
       (list->
        {}
        (->> all-presets
             (vals)
             (map
              (fn [preset]
                [preset
                 (let [selected? (contains? (set presets) (:id preset))]
                   (comp-preset
                    preset
                    selected?
                    (fn [e d! m!]
                      (if selected?
                        (handle-op :remove (:id preset) d!)
                        (handle-op :add (:id preset) d!)))))])))))))))

(defcomp
 comp-presets
 (states presets all-presets template-id path)
 (div
  {}
  (div
   {:style ui/row-middle}
   (<> "Presets" style/field-label)
   (=< 8 nil)
   (cursor-> :edit comp-presets-picker states presets all-presets template-id path))
  (list->
   {:style (merge ui/row {:padding "0 8px"})}
   (->> presets
        (map
         (fn [preset-id]
           [preset-id
            (let [preset (get all-presets preset-id)]
              (comp-preset
               preset
               false
               (fn [e d! m!]
                 (d!
                  :template/node-preset
                  {:template-id template-id, :path path, :op :remove, :value (:id preset)}))))]))))))
