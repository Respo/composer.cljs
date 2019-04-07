
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

(def builtin-presets
  {"Fonts" [:font-code :font-fancy :font-normal],
   "Layouts" [:expand :fullscreen :base-padding],
   "Features" [:scroll :global]})

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
            :line-height "24px"}
           (if selected? {:background-color (hsl 200 80 60)})),
   :on-click on-click}
  (<> (name preset))))

(defcomp
 comp-presets-picker
 (states presets template-id path)
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
      (list->
       {}
       (->> builtin-presets
            (map
             (fn [[group-name xs]]
               [group-name
                (div
                 {:style ui/column}
                 (=< nil 16)
                 (div
                  {}
                  (<>
                   group-name
                   {:font-family ui/font-fancy, :font-size 18, :font-weight 300}))
                 (list->
                  {:style ui/row}
                  (->> xs
                       (map
                        (fn [preset]
                          [preset
                           (let [selected? (contains? (set presets) preset)]
                             (comp-preset
                              preset
                              selected?
                              (fn [e d! m!]
                                (if selected?
                                  (handle-op :remove preset d!)
                                  (handle-op :add preset d!)))))])))))]))))))))

(defcomp
 comp-presets
 (states presets template-id path)
 (div
  {}
  (div
   {:style ui/row-middle}
   (<> "Presets" style/field-label)
   (=< 8 nil)
   (cursor-> :edit comp-presets-picker states presets template-id path))
  (list->
   {:style (merge ui/row {:padding "0 8px"})}
   (->> presets
        (map
         (fn [preset]
           [preset
            (comp-preset
             preset
             false
             (fn [e d! m!]
               (d!
                :template/node-preset
                {:template-id template-id, :path path, :op :remove, :value preset})))]))))))
