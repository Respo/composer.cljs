
(ns app.comp.workspace
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]))

(defcomp
 comp-workspace
 ()
 (div
  {:style (merge ui/row)}
  (div {:style (merge {:width 320})} (<> "Components"))
  (div {:style ui/flex} (<> "content"))
  (div {:style {:width 240}} (<> "Operations"))))
