
(ns app.comp.type-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core :refer [defcomp cursor-> <> action-> span div]]
            [app.config :as config]
            [respo-alerts.comp.alerts :refer [comp-select]]
            [app.style :as style]))

(def node-types
  [{:value :box, :display "Box"}
   {:value :space, :display "Space"}
   {:value :button, :display "Button"}
   {:value :link, :display "Link"}
   {:value :icon, :display "Icon"}
   {:value :text, :display "Text"}
   {:value :some, :display "Some"}
   {:value :template, :display "Template"}
   {:value :list, :display "List"}
   {:value :slot, :display "Slot"}
   {:value :input, :display "Input"}])

(defcomp
 comp-type-picker
 (states template-id focused-path markup)
 (div
  {:style ui/row-middle}
  (<> "Node Type:" style/field-label)
  (=< 8 nil)
  (cursor->
   :type
   comp-select
   states
   (:type markup)
   node-types
   {}
   (fn [result d! m!]
     (if (some? result)
       (d! :template/node-type {:template-id template-id, :path focused-path, :type result}))))))
