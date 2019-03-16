
(ns composer.comp.overflow
  (:require [hsl.core :refer [hsl]]
            [composer.schema :as schema]
            [respo-ui.core :as ui]
            [respo.core :refer [defcomp list-> <> span div button]]
            [respo.comp.space :refer [=<]]
            [composer.config :as config]
            [composer.util :refer [neaten-templates]]
            [composer.core :refer [render-markup]]))

(defcomp
 comp-overview
 (templates)
 (let [tmpls (neaten-templates templates)]
   (list->
    {:style (merge
             ui/flex
             {:padding "8px 16px 160px 16px",
              :overflow :auto,
              :background-color (hsl 0 0 94)})}
    (->> templates
         (map
          (fn [[k template]]
            [k
             (let [style-container {:background-color (hsl 0 0 100),
                                    :border "1px solid #ddd",
                                    :min-width (or (:width template) 240),
                                    :min-height (or (:height template) 60)}]
               (div
                {:style (merge ui/row {:margin "16px 0px"})}
                (div
                 {:style {:font-family ui/font-fancy, :font-size 20, :min-width 120}}
                 (<> (:name template)))
                (if (empty? (:mocks template))
                  (div
                   {:style {:padding 8}}
                   (div
                    {:style style-container}
                    (render-markup
                     (:markup template)
                     {:data nil, :templates tmpls, :level 0, :hide-popup? true}
                     (fn [d! op param options] (println op param (pr-str options))))))
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
                              {:style style-container}
                              (render-markup
                               (:markup template)
                               {:data (:data mock),
                                :templates tmpls,
                                :level 0,
                                :hide-popup? true}
                               (fn [d! op param options]
                                 (println op param (pr-str options)))))
                             (div
                              {:style {:margin-left 8,
                                       :color (hsl 0 0 70),
                                       :font-size 13,
                                       :font-family ui/font-fancy}}
                              (<> (:name mock))))])))))))]))))))
