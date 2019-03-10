
(ns app.comp.overflow
  (:require [hsl.core :refer [hsl]]
            [app.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [app.config :as config]
            [app.util :refer [neaten-templates]]
            [respo-composer.core :refer [render-markup]]))

(defcomp
 comp-overview
 (templates)
 (let [tmpls (neaten-templates templates)]
   (list->
    {:style (merge
             ui/flex
             {:padding "8px 16px", :overflow :auto, :background-color (hsl 0 0 94)})}
    (->> templates
         (map
          (fn [[k template]]
            [k
             (div
              {:style (merge ui/row {:margin "16px 0px"})}
              (div
               {:style {:font-family ui/font-fancy, :font-size 20, :min-width 120}}
               (<> (:name template)))
              (list->
               {:style (merge ui/flex {})}
               (->> (:mocks template)
                    (map
                     (fn [[k mock]]
                       [k
                        (div
                         {:style (merge
                                  ui/row
                                  {:display :inline-flex,
                                   :margin-right 32,
                                   :padding 8,
                                   :vertical-align :top})}
                         (div
                          {:style {:background-color (hsl 0 0 100),
                                   :border "1px solid #ddd",
                                   :min-width 200}}
                          (render-markup
                           (:markup template)
                           {:data (:data mock), :templates tmpls, :level 0}
                           (fn [d! op param options] (println op param (pr-str options)))))
                         (div
                          {:style {:margin-left 8,
                                   :color (hsl 0 0 70),
                                   :font-size 13,
                                   :font-family ui/font-fancy}}
                          (<> (:name mock))))])))))]))))))
