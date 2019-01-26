
(ns app.comp.mock-data
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]))

(defcomp comp-mock-data () (div {:style (merge ui/row)} (<> "Mock data")))
