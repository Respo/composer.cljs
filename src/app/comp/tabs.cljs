
(ns app.comp.tabs
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-tabs
 (tabs selected-tab on-select)
 (list->
  {:style (merge ui/row-middle {:padding "0 8px", :font-family ui/font-fancy})}
  (->> tabs
       (map
        (fn [tab]
          [(:value tab)
           (div
            {:style (merge
                     {:padding "0 8px", :cursor :pointer, :color (hsl 0 0 60)}
                     (if (= (:value tab) selected-tab) {:color :black})),
             :on-click (fn [e d! m!] (on-select tab d! m!))}
            (<> (:display tab)))])))))
