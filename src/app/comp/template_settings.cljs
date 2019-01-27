
(ns app.comp.template-settings
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]))

(defcomp
 comp-template-settings
 ()
 (div {:style (merge ui/flex ui/row)} (<> "Template settings")))
