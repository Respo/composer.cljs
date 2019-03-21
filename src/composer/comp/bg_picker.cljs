
(ns composer.comp.bg-picker
  (:require [hsl.core :refer [hsl]]
            [respo-ui.core :as ui]
            [respo.comp.space :refer [=<]]
            [respo.core
             :refer
             [defcomp <> action-> list-> cursor-> span div input button a]]
            [composer.config :as config]
            [inflow-popup.comp.popup :refer [comp-popup]]
            [composer.style :as style]))

(defcomp
 comp-color-panel
 (states colors initial-color set-color! on-toggle)
 (let [state (or (:data states) {:text initial-color})
       grouped-colors (group-by :group (vals colors))]
   (div
    {:style {:width 360}}
    (div {} (<> "Pick a color" {:font-family ui/font-fancy}))
    (list->
     {}
     (->> grouped-colors
          (map
           (fn [[group-name colors]]
             [group-name
              (div
               {}
               (div {} (<> (or group-name "theme") {:font-family ui/font-fancy}))
               (list->
                {:style ui/row}
                (->> colors
                     (map
                      (fn [color]
                        [(:id color)
                         (div
                          {:style (merge
                                   ui/center
                                   {:background-color (:color color),
                                    :width 32,
                                    :height 32,
                                    :margin 4,
                                    :cursor :pointer,
                                    :border "1px solid #ddd"}),
                           :on-click (fn [e d! m!] (set-color! (:color color) d!))}
                          (<> (:name color) {:color :white, :font-size 10}))])))))]))))
    (div
     {}
     (a
      {:style ui/link,
       :inner-text "Add colors",
       :on-click (fn [e d! m!] (d! :router/change {:name :settings}))})))))

(defcomp
 comp-bg-picker
 (states template-id path markup colors)
 (let [bg-color (or (get-in markup [:style "background-color"]) (hsl 0 0 100))
       set-color! (fn [color d!]
                    (d!
                     :template/set-node-style
                     {:template-id template-id,
                      :path path,
                      :property "background-color",
                      :value color}))]
   (div
    {:style ui/row-middle}
    (<> "Background:" style/field-label)
    (=< 8 nil)
    (cursor->
     :bg-color
     comp-popup
     states
     {:trigger (div
                {:style {:width 24,
                         :height 24,
                         :background-color bg-color,
                         :border "1px solid #ddd"}})}
     (fn [on-toggle]
       (cursor-> :panel comp-color-panel states colors bg-color set-color! on-toggle))))))

(defcomp
 comp-font-picker
 (states template-id path markup colors)
 (let [init-color (or (get-in markup [:style "color"]) (hsl 0 0 100))
       set-color! (fn [color d!]
                    (d!
                     :template/set-node-style
                     {:template-id template-id, :path path, :property "color", :value color}))]
   (div
    {:style ui/row-middle}
    (<> "Font color:" style/field-label)
    (=< 8 nil)
    (cursor->
     :font-color
     comp-popup
     states
     {:trigger (div
                {:style {:width 24,
                         :height 24,
                         :background-color init-color,
                         :border "1px solid #ddd"}})}
     (fn [on-toggle]
       (cursor-> :panel comp-color-panel states colors init-color set-color! on-toggle))))))
