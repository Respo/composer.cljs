
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
  {:style ui/row-middle}
  (->> tabs
       (map
        (fn [tab]
          [(:value tab)
           (div
            {:style (merge
                     {:padding "0 8px",
                      :border (<< "1px solid ~(hsl 0 0 90)"),
                      :cursor :pointer,
                      :border-radius "4px",
                      :color (hsl 0 0 60)}
                     (if (= (:value tab) selected-tab) {:color :black})),
             :on-click (fn [e d! m!] (on-select tab d! m!))}
            (<> (:display tab)))])))))
