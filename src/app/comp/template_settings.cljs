
(ns app.comp.template-settings
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> cursor-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo-alerts.comp.alerts :refer [comp-confirm comp-prompt]]
            [clojure.string :as string]
            ["copy-text-to-clipboard" :as copy!]))

(defcomp
 comp-template-settings
 (states template)
 (div
  {:style (merge ui/flex ui/column {:padding "8px"})}
  (div {:style {:font-family ui/font-fancy, :font-size 20}} (<> "Template settings"))
  (div
   {}
   (cursor->
    :rename
    comp-prompt
    states
    {:trigger (button {:style ui/button, :inner-text "Change name"}),
     :initial (:name template),
     :text "Change the name"}
    (fn [result d! m!]
      (when-not (string/blank? result)
        (d! :template/rename {:id (:id template), :name result}))))
   (=< 8 nil)
   (cursor->
    :remove
    comp-confirm
    states
    {:trigger (button {:style ui/button, :inner-text "Remove"}), :text "Sure to remove?"}
    (fn [e d! m!] (d! :template/remove (:id template))))
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "Copy",
     :on-click (fn [e d! m!] (copy! (pr-str (:markup template))))}))))
