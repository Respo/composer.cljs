
(ns app.comp.editor
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt]]))

(defcomp
 comp-markup
 (markup path focused-path)
 (div
  {:style {}}
  (<> (:type markup))
  (list->
   {:style {:padding-left 16}}
   (->> (:children markup)
        (map-val
         (fn [child-markup]
           (div {} (comp-markup child-markup (conj path (:id markup)) focused-path))))))))

(defcomp
 comp-operations
 (template-id focused-path)
 (div
  {:style (merge ui/column {:width 240})}
  (div {} (<> "Operations"))
  (div
   {}
   (button
    {:style ui/button,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path (or focused-path [])}))})
   (=< 8 nil)
   (button {:style ui/button, :inner-text "Change Type"}))
  (=< nil 16)
  (div {} (<> "Changes"))
  (div
   {}
   (button
    {:style ui/button,
     :inner-text "Remove",
     :on-click (fn [e d! m!] (d! :template/remove-markup nil))}))))

(defcomp
 comp-editor
 (template focused-path)
 (div
  {:style (merge ui/flex ui/row)}
  (div
   {:style ui/flex}
   (comp-markup (:markup template) [] focused-path)
   (comp-inspect "Markup" (:markup template) {}))
  (comp-operations (:id template) focused-path)))
