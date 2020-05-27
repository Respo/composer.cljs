
(ns composer.comp.template-settings
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> >> <> span div button]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [respo-alerts.core :refer [comp-confirm comp-prompt]]
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
   (comp-prompt
    (>> states :rename)
    {:trigger (button {:style ui/button, :inner-text "Change name"}),
     :initial (:name template),
     :text "Change the name"}
    (fn [result d!]
      (when-not (string/blank? result)
        (d! :template/rename {:id (:id template), :name result}))))
   (=< 8 nil)
   (button
    {:style ui/button,
     :inner-text "Copy",
     :on-click (fn [e d!] (copy! (pr-str (:markup template))))})
   (=< 8 nil)
   (comp-confirm
    (>> states :remove)
    {:trigger (button
               {:style (merge ui/button {:color :red, :border-color :red}),
                :inner-text "Remove"}),
     :text "Sure to remove?"}
    (fn [e d!] (d! :template/remove (:id template)))))))
