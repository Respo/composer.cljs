
(ns composer.comp.settings
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp cursor-> list-> <> span div button input]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [feather.core :refer [comp-i]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-confirm comp-prompt]]
            [clojure.string :as string]
            [composer.util.dom :refer [focus-element!]]
            [composer.comp.colors-manager :refer [comp-colors-manager]]
            [composer.comp.tabs :refer [comp-tabs]]
            [composer.comp.presets-manager :refer [comp-presets-manager]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(def setting-tabs
  [{:value :colors, :display "Color"} {:value :presets, :display "Presets"}])

(defcomp
 comp-settings
 (states settings)
 (let [state (or (:data states) {:tab :colors})]
   (div
    {:style (merge ui/flex ui/column)}
    (div
     {:style {:border-bottom (str "1px solid " (hsl 0 0 90))}}
     (comp-tabs
      setting-tabs
      (:tab state)
      (fn [result d! m!] (m! (assoc state :tab (:value result))))))
    (case (:tab state)
      :colors (cursor-> :colors comp-colors-manager states (:color-groups settings))
      :presets (cursor-> :presets comp-presets-manager states (:presets settings))
      (<> "No tab selected.")))))
