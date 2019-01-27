
(ns app.comp.editor
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]))

(defcomp
 comp-editor
 (template)
 (div
  {:style (merge ui/flex ui/row)}
  (div {:style ui/flex} (<> "Template"))
  (div
   {:style (merge ui/column {:width 240})}
   (div {} (<> "Operations"))
   (div
    {}
    (button {:style ui/button, :inner-text "Append"})
    (button {:style ui/button, :inner-text "Type"})))))
