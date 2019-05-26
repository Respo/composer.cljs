
(ns composer.comp.presets-manager
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button input a]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.util.list :refer [map-val]]
            [composer.core :refer [render-markup]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]
            [clojure.string :as string]
            [feather.core :refer [comp-icon comp-i]]))

(defcomp
 comp-presets-manager
 (states presets)
 (div
  {:style (merge ui/expand {:padding 16})}
  (div
   {:style {:font-family ui/font-fancy, :font-size 20, :color (hsl 0 0 70)}}
   (<> "Presets")
   (=< 8 nil)
   (cursor->
    :rename
    comp-prompt
    states
    {:trigger (comp-i :plus 14 (hsl 200 100 80)),
     :style {:display :inline-block},
     :text "Name for a preset:"}
    (fn [result d! m!] (d! :settings/create-preset result))))
  (div
   {:style ui/row}
   (list->
    {:style (merge ui/column {:min-width 80})}
    (->> presets (map-val (fn [preset] (div {} (<> (:name preset)))))))
   (=< 8 nil)
   (<> "DATA"))))
