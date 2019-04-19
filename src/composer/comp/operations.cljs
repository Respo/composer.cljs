
(ns composer.comp.operations
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button a pre]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm comp-select]]
            [composer.util :refer [path-with-children]]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [composer.style :as style]
            [bisection-key.core :as bisection]))

(defcomp
 comp-operations
 (states template-id focused-path)
 (div
  {:style (merge ui/row {:padding "0 8px"})}
  (div {} (<> "Operations:" style/field-label))
  (div
   {:style (merge ui/flex)}
   (a
    {:style style/link,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path focused-path})
       (d! :router/move-append nil))})
   (a
    {:style style/link,
     :inner-text "After",
     :on-click (fn [e d! m!]
       (d! :template/after-markup {:template-id template-id, :path focused-path})
       (d! :router/move-after nil))})
   (a
    {:style style/link,
     :inner-text "Prepend",
     :on-click (fn [e d! m!]
       (d! :template/prepend-markup {:template-id template-id, :path focused-path})
       (d! :router/move-prepend nil))})
   (a
    {:style style/link,
     :inner-text "Before",
     :on-click (fn [e d! m!]
       (d! :template/before-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (a {:style ui/link, :inner-text "Remove"})}
    (fn [e d! m!]
      (d! :template/remove-markup {:template-id template-id, :path focused-path})
      (d! :session/focus-to {:path (vec (butlast focused-path))})))
   (a
    {:style style/link,
     :inner-text "Wrap",
     :on-click (fn [e d! m!]
       (d! :template/wrap-markup {:template-id template-id, :path focused-path})
       (d! :session/focus-to {:path (conj focused-path bisection/mid-id)}))})
   (a
    {:style style/link,
     :inner-text "Spread",
     :on-click (fn [e d! m!]
       (d! :template/spread-markup {:template-id template-id, :path focused-path})
       (d! :router/move-before nil))})
   (a
    {:style style/link,
     :inner-text "Copy",
     :on-click (fn [e d! m!]
       (d! :session/copy-markup {:template-id template-id, :path focused-path}))})
   (a
    {:style style/link,
     :inner-text "Paste",
     :on-click (fn [e d! m!]
       (d! :session/paste-markup {:template-id template-id, :path focused-path}))}))))
