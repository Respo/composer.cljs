
(ns app.comp.editor
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.comp.inspect :refer [comp-inspect]]
            [respo.util.list :refer [map-val]]
            [respo-alerts.comp.alerts :refer [comp-prompt comp-confirm comp-select]]
            [app.util :refer [path-with-children]]))

(def node-types
  [{:value :box, :display "Box"}
   {:value :text, :display "Text"}
   {:value :space, :display "Space"}
   {:value :icon, :display "Icon"}
   {:value :if, :display "if expression"}
   {:value :value, :display "value expression"}])

(defcomp
 comp-kind-picker
 (states template-id focused-path markup)
 (div
  {:style ui/row-middle}
  (<> "Node Type:")
  (=< 8 nil)
  (cursor->
   :type
   comp-select
   states
   (:type markup)
   node-types
   {}
   (fn [result d! m!]
     (d! :template/node-type {:template-id template-id, :path focused-path, :type result})))))

(defcomp
 comp-markup
 (markup path focused-path)
 (div
  {:style {}}
  (div
   {:style (merge
            {:display :inline-block,
             :cursor :pointer,
             :padding "0 8px",
             :background-color (hsl 0 0 88),
             :color :white,
             :border-radius "8px"}
            (if (= path focused-path) {:background-color (hsl 200 80 70)})),
    :on-click (fn [e d! m!] (d! :router/set-focused-path path))}
   (<> (:type markup)))
  (list->
   {:style {:padding-left 16}}
   (->> (:children markup)
        (sort-by (fn [[k child-markup]] k))
        (map-val
         (fn [child-markup]
           (div {} (comp-markup child-markup (conj path (:id child-markup)) focused-path))))))))

(defcomp
 comp-operations
 (states template-id focused-path)
 (div
  {:style (merge ui/column {:width 240})}
  (div {} (<> "Operations"))
  (div
   {:style {}}
   (button
    {:style ui/button,
     :inner-text "Append",
     :on-click (fn [e d! m!]
       (d! :template/append-markup {:template-id template-id, :path focused-path}))})
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "After",
     :on-click (fn [e d! m!]
       (d! :template/after-markup {:template-id template-id, :path focused-path}))})
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "Prepend",
     :on-click (fn [e d! m!]
       (d! :template/prepend-markup {:template-id template-id, :path focused-path}))})
   (button
    {:style ui/button,
     :inner-text "Before",
     :on-click (fn [e d! m!]
       (d! :template/before-markup {:template-id template-id, :path focused-path}))})
   (=< 8 nil)
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (button {:style ui/button, :inner-text "Remove"})}
    (fn [e d! m!]
      (d! :template/remove-markup {:template-id template-id, :path focused-path})
      (d! :router/set-focused-path (vec (butlast focused-path))))))
  (=< nil 16)
  (div {} (<> "Changes"))))

(defcomp
 comp-editor
 (states template focused-path)
 (div
  {:style (merge ui/flex ui/row)}
  (div
   {:style ui/flex}
   (comp-markup (:markup template) [] focused-path)
   (comp-inspect "Markup" (:markup template) {}))
  (div
   {:style {}}
   (let [child (get-in (:markup template) (interleave (repeat :children) focused-path))]
     (cursor-> :kind comp-kind-picker states (:id template) focused-path child))
   (cursor-> :operations comp-operations states (:id template) (or focused-path [])))))
