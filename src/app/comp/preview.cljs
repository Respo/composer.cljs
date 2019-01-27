
(ns app.comp.preview
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [respo.util.list :refer [map-val]])
  (:require-macros [clojure.core.strint :refer [<<]]))

(defcomp
 comp-preview
 (templates pointer)
 (div
  {:style (merge ui/flex ui/row {:padding "0 8px"})}
  (div
   {:style (merge ui/column {:width 240, :padding 8})}
   (div {:style {:font-family ui/font-fancy}} (<> "Templates"))
   (list->
    {}
    (->> templates
         (map-val
          (fn [template]
            (div
             {:style (merge
                      {:line-height "40px", :cursor :pointer, :padding "0 8px"}
                      (if (= pointer (:id template)) {:background-color (hsl 0 0 90)})),
              :on-click (fn [e d! m!] (d! :router/set-pointer (:id template)))}
             (<> (:name template))))))))
  (div
   {:style (merge ui/flex ui/column)}
   (div
    {:style (merge ui/flex ui/center {:background-color (hsl 0 0 88), :overflow :auto})}
    (div {:style {:background-color :white}} (<> "TODO PREVIEW")))
   (div {:style ui/row-parted} (span {}) (div {} (<> "TODO MOCKs"))))))
