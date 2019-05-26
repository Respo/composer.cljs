
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
            [composer.comp.colors-manager :refer [comp-colors-manager]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-settings
 (states settings)
 (div
  {:style {:padding 16}}
  (cursor-> :colors comp-colors-manager states (:color-groups settings))))
